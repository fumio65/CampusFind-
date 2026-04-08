package com.campusfind.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.data.repository.FirestoreClaimRepositoryImpl
import com.campusfind.data.repository.FirestoreTipRepositoryImpl
import com.campusfind.domain.model.Claim
import com.campusfind.domain.model.ClaimReply
import com.campusfind.domain.model.ClaimStatus
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import com.campusfind.domain.model.Tip
import com.campusfind.domain.repository.ClaimRepository
import com.campusfind.domain.repository.LostItemRepository
import com.campusfind.domain.repository.TipRepository
import com.campusfind.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val lostItemRepository: LostItemRepository,
    private val userRepository: UserRepository,
    private val tipRepository: TipRepository,
    private val claimRepository: ClaimRepository,
    private val sessionManager: SessionManager,
    // ── Firestore sync — pulls remote tips/claims into Room ───────────────
    private val firestoreTipRepository: FirestoreTipRepositoryImpl,
    private val firestoreClaimRepository: FirestoreClaimRepositoryImpl
) : ViewModel() {

    companion object {
        const val MAX_TIPS_PER_ITEM          = 30
        const val MAX_TIPS_PER_USER_PER_ITEM = 1
        const val MAX_REPLIES_PER_USER_PER_ITEM = 5
        const val MIN_TIP_LENGTH             = 2
        const val MAX_TIP_LENGTH             = 200
        const val REPLY_COOLDOWN_MS          = 5000L
        const val MAX_CLAIM_REPLIES_TOTAL    = 2  // 1 from reporter + 1 from finder
    }

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var currentItemId: String = ""
    private var lastReplyTime: Long   = 0
    private var tipsJob: Job?         = null
    private var claimsJob: Job?       = null
    private var periodicSyncJob: Job? = null
    private val claimReplyJobs        = mutableMapOf<String, Job>()

    val hasPendingClaim: Boolean
        get() = _uiState.value.claims.any { it.status == ClaimStatus.PENDING }

    // ── Load item — local Room first, then sync Firestore ─────────────────

    fun loadItem(itemId: String) {
        currentItemId = itemId
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val item = lostItemRepository.getItemById(itemId)
                if (item == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Item not found") }
                    return@launch
                }

                val reporter      = userRepository.getUserById(item.reportedBy)
                val currentUserId = sessionManager.currentUserId
                val isOwner       = currentUserId == item.reportedBy
                val claimCount    = claimRepository.getClaimCountByItemId(itemId)

                _uiState.update {
                    it.copy(
                        item         = item,
                        reporterName = reporter?.fullName,
                        isOwner      = isOwner,
                        isLoading    = false,
                        claimCount   = claimCount
                    )
                }

                // Start observing Room (reactive — updates UI automatically)
                observeTips()
                observeClaims()

                // Initial sync — pull latest tips + claims from Firestore into Room
                launch(Dispatchers.IO) {
                    firestoreTipRepository.syncTipsFromFirestore(itemId)
                    firestoreClaimRepository.syncClaimsFromFirestore(itemId)
                }

                // Periodic re-sync every 5 seconds while detail screen is open
                // This ensures lhester sees Mark's reply without navigating away
                startPeriodicSync(itemId)

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // ── Periodic sync while screen is open ────────────────────────────────
    // Polls Firestore every 5 seconds so both parties see new replies
    // without needing to navigate away and back

    private fun startPeriodicSync(itemId: String) {
        periodicSyncJob?.cancel()
        periodicSyncJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                kotlinx.coroutines.delay(5_000)
                try {
                    firestoreTipRepository.syncTipsFromFirestore(itemId)
                    firestoreClaimRepository.syncClaimsFromFirestore(itemId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // ── Stop periodic sync when ViewModel is cleared ───────────────────────

    override fun onCleared() {
        super.onCleared()
        periodicSyncJob?.cancel()
    }

    // ── Observe tips from Room ─────────────────────────────────────────────

    private fun observeTips() {
        tipsJob?.cancel()
        tipsJob = viewModelScope.launch {
            tipRepository.getTipsByItemId(currentItemId)
                .collect { tips ->
                    val userId       = sessionManager.currentUserId
                    val hasUserTipped = tips.any { it.authorId == userId && !it.isReply }
                    _uiState.update {
                        it.copy(
                            tips          = tips,
                            tipCount      = tips.count { !it.isReply },
                            hasUserTipped = hasUserTipped
                        )
                    }
                }
        }
    }

    // ── Observe claims from Room ───────────────────────────────────────────

    private fun observeClaims() {
        claimsJob?.cancel()
        claimReplyJobs.clear()

        claimsJob = viewModelScope.launch {
            claimRepository.getClaimsByItem(currentItemId)
                .collect { claims ->
                    claims.forEach { claim ->
                        if (claim.status == ClaimStatus.APPROVED &&
                            !claimReplyJobs.containsKey(claim.id)) {
                            claimReplyJobs[claim.id] = launch {
                                claimRepository.getRepliesByClaimId(claim.id)
                                    .collect { replies ->
                                        _uiState.update { state ->
                                            val updatedReplies = state.claimReplies.toMutableMap()
                                            val updatedCounts  = state.claimReplyCounts.toMutableMap()
                                            updatedReplies[claim.id] = replies
                                            updatedCounts[claim.id]  = replies.size
                                            state.copy(
                                                claimReplies     = updatedReplies,
                                                claimReplyCounts = updatedCounts
                                            )
                                        }
                                    }
                            }
                        }
                    }

                    // Cancel reply jobs for removed claims
                    val currentIds = claims.map { it.id }.toSet()
                    claimReplyJobs.keys
                        .filter { it !in currentIds }
                        .forEach { id -> claimReplyJobs.remove(id)?.cancel() }

                    _uiState.update { it.copy(claims = claims, claimCount = claims.size) }
                }
        }
    }

    // ── Submit tip ─────────────────────────────────────────────────────────

    fun submitTip(message: String) {
        val currentState = _uiState.value

        if (currentState.hasUserTipped) {
            _uiState.update {
                it.copy(showLimitDialog = "You've already left a tip on this item!\n\nYou can only leave one tip per item, but you can reply to the reporter's responses. 💬")
            }
            return
        }

        val trimmed = message.trim()

        if (trimmed.length < MIN_TIP_LENGTH) {
            _uiState.update {
                it.copy(showLimitDialog = "Your tip is too short!\n\nPlease write at least $MIN_TIP_LENGTH characters to help others. 💬")
            }
            return
        }

        if (trimmed.length > MAX_TIP_LENGTH) {
            _uiState.update {
                it.copy(showLimitDialog = "Your tip is too long!\n\nPlease keep it under $MAX_TIP_LENGTH characters. ✂️")
            }
            return
        }

        if (currentState.tipCount >= MAX_TIPS_PER_ITEM) {
            _uiState.update {
                it.copy(showLimitDialog = "This item has reached the maximum of $MAX_TIPS_PER_ITEM tips.\n\nThis limit helps us keep the app free! 🎉")
            }
            return
        }

        viewModelScope.launch {
            val userId = sessionManager.currentUserId
            if (userId == null) {
                _uiState.update { it.copy(error = "You must be logged in to leave a tip") }
                return@launch
            }
            val result = tipRepository.submitTip(
                itemId   = currentState.item?.id ?: return@launch,
                authorId = userId,
                message  = trimmed
            )
            if (result.isFailure) {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message) }
            }
        }
    }

    // ── Submit tip reply ───────────────────────────────────────────────────

    fun submitReply(parentTipId: String, message: String) {
        val currentState = _uiState.value
        val trimmed      = message.trim()
        val userId       = sessionManager.currentUserId

        if (userId != null) {
            if (currentState.isOwner) {
                // Reporter: max 5 replies across all tips on this item
                val userReplyCount = currentState.tips.count { it.authorId == userId && it.isReply }
                if (userReplyCount >= MAX_REPLIES_PER_USER_PER_ITEM) {
                    _uiState.update {
                        it.copy(showLimitDialog = "You've reached the maximum of $MAX_REPLIES_PER_USER_PER_ITEM replies on this item.\n\nThis limit helps us keep the app free for everyone! 🎉")
                    }
                    return
                }
            } else {
                // Tipper: only 1 reply per tip thread (after reporter replies)
                val alreadyRepliedToThisTip = currentState.tips.any {
                    it.authorId == userId && it.isReply && it.parentTipId == parentTipId
                }
                if (alreadyRepliedToThisTip) {
                    _uiState.update {
                        it.copy(showLimitDialog = "You can only reply once per tip thread. 💬")
                    }
                    return
                }
            }
        }

        val now = System.currentTimeMillis()
        if (now - lastReplyTime < REPLY_COOLDOWN_MS) {
            val secondsLeft = ((REPLY_COOLDOWN_MS - (now - lastReplyTime)) / 1000).toInt()
            _uiState.update { it.copy(error = "Please wait $secondsLeft seconds before replying again") }
            return
        }

        if (trimmed.length < MIN_TIP_LENGTH) {
            _uiState.update {
                it.copy(showLimitDialog = "Your reply is too short!\n\nPlease write at least $MIN_TIP_LENGTH characters. 💬")
            }
            return
        }

        if (trimmed.length > MAX_TIP_LENGTH) {
            _uiState.update {
                it.copy(showLimitDialog = "Your reply is too long!\n\nPlease keep it under $MAX_TIP_LENGTH characters. ✂️")
            }
            return
        }

        viewModelScope.launch {
            if (userId == null) {
                _uiState.update { it.copy(error = "You must be logged in to reply") }
                return@launch
            }
            val result = tipRepository.submitReply(
                parentTipId = parentTipId,
                itemId      = currentState.item?.id ?: return@launch,
                authorId    = userId,
                message     = trimmed
            )
            if (result.isSuccess) {
                lastReplyTime = System.currentTimeMillis()
            } else {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message) }
            }
        }
    }

    // ── Mark as Found ──────────────────────────────────────────────────────

    fun markAsFound(itemId: String, item: LostItem) {
        if (item.reportedBy != sessionManager.currentUserId) {
            _uiState.update { it.copy(error = "You can only mark your own items as found") }
            return
        }
        viewModelScope.launch {
            val result = lostItemRepository.updateItemStatus(id = itemId, status = ItemStatus.FOUND)
            if (result.isSuccess) {
                loadItem(itemId)
            } else {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message ?: "Failed to mark as found") }
            }
        }
    }

    // ── Submit claim inline ────────────────────────────────────────────────

    fun submitClaimInline(location: String, photoUri: String?) {
        if (hasPendingClaim) {
            _uiState.update { it.copy(error = "A claim is already being reviewed.") }
            return
        }

        val trimmed = location.trim()
        if (trimmed.length < 10) {
            _uiState.update { it.copy(error = "Please provide more details (at least 10 characters)") }
            return
        }

        if (photoUri.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Photo proof is required") }
            return
        }

        viewModelScope.launch {
            val userId = sessionManager.currentUserId
            if (userId == null) {
                _uiState.update { it.copy(error = "You must be logged in") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true) }
            val result = claimRepository.submitClaim(
                itemId   = currentItemId,
                finderId = userId,
                message  = trimmed,
                photoUrl = photoUri
            )
            _uiState.update { it.copy(isLoading = false) }
            if (result.isFailure) {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message ?: "Failed to submit claim") }
            }
        }
    }

    // ── Reply to claim (reporter + claimer only, 1 each) ───────────────────

    fun replyToClaim(claimId: String, message: String) {
        val trimmed       = message.trim()
        val currentUserId = sessionManager.currentUserId

        if (trimmed.length < 2 || trimmed.length > 200) {
            _uiState.update { it.copy(error = "Message must be 2-200 characters") }
            return
        }

        if (currentUserId == null) {
            _uiState.update { it.copy(error = "You must be logged in") }
            return
        }

        val replies            = _uiState.value.claimReplies[claimId] ?: emptyList()
        val userAlreadyReplied = replies.any { it.authorId == currentUserId }

        if (userAlreadyReplied) {
            _uiState.update {
                it.copy(showLimitDialog = "You can only send 1 message per claim.\n\nUse Messenger for further coordination! 💬")
            }
            return
        }

        if (replies.size >= MAX_CLAIM_REPLIES_TOTAL) {
            _uiState.update {
                it.copy(showLimitDialog = "2-message limit reached.\n\nUse Messenger to continue the conversation! 💬")
            }
            return
        }

        viewModelScope.launch {
            val result = claimRepository.submitClaimReply(
                claimId  = claimId,
                authorId = currentUserId,
                message  = trimmed
            )
            if (result.isFailure) {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message ?: "Failed to send reply") }
            }
        }
    }

    // ── Approve / Reject claim ─────────────────────────────────────────────

    fun approveClaim(claimId: String) {
        if (!_uiState.value.isOwner) {
            _uiState.update { it.copy(error = "Only the reporter can approve claims") }
            return
        }
        viewModelScope.launch {
            val result = claimRepository.updateClaimStatus(claimId, ClaimStatus.APPROVED)
            if (result.isSuccess) loadItem(currentItemId)
            else _uiState.update { it.copy(error = result.exceptionOrNull()?.message ?: "Failed to approve claim") }
        }
    }

    fun rejectClaim(claimId: String) {
        if (!_uiState.value.isOwner) {
            _uiState.update { it.copy(error = "Only the reporter can reject claims") }
            return
        }
        viewModelScope.launch {
            val result = claimRepository.updateClaimStatus(claimId, ClaimStatus.REJECTED)
            if (result.isFailure) {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message ?: "Failed to reject claim") }
            }
        }
    }

    // ── Delete item ────────────────────────────────────────────────────────

    fun deleteItem(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                lostItemRepository.deleteItem(currentItemId)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete item: ${e.message}") }
            }
        }
    }

    // ── Withdraw claim ─────────────────────────────────────────────────────

    fun withdrawClaim(claimId: String) {
        viewModelScope.launch {
            val result = claimRepository.deleteClaim(claimId)
            if (result.isFailure) {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message ?: "Failed to withdraw claim") }
            }
        }
    }

    // ── UI helpers ─────────────────────────────────────────────────────────

    fun getCurrentUserId(): String? = sessionManager.currentUserId

    fun getReplyCount(claimId: String): Int =
        _uiState.value.claimReplyCounts[claimId] ?: 0

    fun getReplies(claimId: String): List<ClaimReply> =
        _uiState.value.claimReplies[claimId] ?: emptyList()

    fun hasUserRepliedToClaim(claimId: String): Boolean {
        val currentUserId = sessionManager.currentUserId ?: return false
        return (_uiState.value.claimReplies[claimId] ?: emptyList()).any { it.authorId == currentUserId }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }

    fun dismissLimitDialog() { _uiState.update { it.copy(showLimitDialog = null) } }

    fun refresh() {
        if (currentItemId.isNotBlank()) {
            loadItem(currentItemId)
        }
    }
}