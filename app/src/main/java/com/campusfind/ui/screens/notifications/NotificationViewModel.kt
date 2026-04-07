package com.campusfind.ui.screens.notifications

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.notifications.AppNotificationManager
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.data.repository.FirestoreClaimRepositoryImpl
import com.campusfind.data.repository.FirestoreLostItemRepositoryImpl
import com.campusfind.data.repository.FirestoreTipRepositoryImpl
import com.campusfind.domain.model.ClaimStatus
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.repository.ClaimRepository
import com.campusfind.domain.repository.LostItemRepository
import com.campusfind.domain.repository.TipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class NotificationType {
    ITEM_FOUND,
    STILL_PENDING,
    NEW_REPORT,
    CLAIM_RECEIVED,
    CLAIM_APPROVED,
    CLAIM_REJECTED,
    CLAIM_REPLY_RECEIVED,
    CLAIM_REPLY_TO_YOU,
    TIP_RECEIVED,
    TIP_REPLY_RECEIVED,
    ITEM_RETURNED;        // ← NEW: notifies approved claimer when item marked FOUND

    fun getIcon(): String = when (this) {
        ITEM_FOUND           -> "✅"
        STILL_PENDING        -> "⏳"
        NEW_REPORT           -> "📢"
        CLAIM_RECEIVED       -> "✋"
        CLAIM_APPROVED       -> "🎉"
        CLAIM_REJECTED       -> "❌"
        CLAIM_REPLY_RECEIVED -> "💬"
        CLAIM_REPLY_TO_YOU   -> "💬"
        TIP_RECEIVED         -> "💡"
        TIP_REPLY_RECEIVED   -> "↩️"
        ITEM_RETURNED        -> "📦"
    }
}

data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestamp: Long,
    val itemId: String? = null,
    val isRead: Boolean = false
)

data class NotificationsUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val lostItemRepository: LostItemRepository,
    private val claimRepository: ClaimRepository,
    private val tipRepository: TipRepository,
    private val sessionManager: SessionManager,
    private val prefs: SharedPreferences,
    private val appNotificationManager: AppNotificationManager,
    private val firestoreLostItemRepository: FirestoreLostItemRepositoryImpl,
    private val firestoreTipRepository: FirestoreTipRepositoryImpl,
    private val firestoreClaimRepository: FirestoreClaimRepositoryImpl
) : ViewModel() {

    private val systemNotifiedIds = mutableSetOf<String>()
    private var isFirstLoad = true

    private val readKey: String by lazy {
        "read_notif_ids_${sessionManager.currentUserId ?: "guest"}"
    }

    private fun getReadIds(): MutableSet<String> =
        prefs.getStringSet(readKey, emptySet())?.toMutableSet() ?: mutableSetOf()

    private fun saveReadId(id: String) {
        val ids = getReadIds().also { it.add(id) }
        prefs.edit().putStringSet(readKey, ids).apply()
    }

    private fun saveAllReadIds(ids: Set<String>) {
        prefs.edit().putStringSet(readKey, ids).apply()
    }

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    val unreadCount: StateFlow<Int> = _uiState
        .map { it.unreadCount }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private var loadJob: Job? = null

    init { loadNotifications() }

    fun loadNotifications() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val currentUserId = sessionManager.currentUserId
            if (currentUserId == null) {
                _uiState.update {
                    it.copy(isLoading = false, notifications = emptyList(), unreadCount = 0)
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            // Step 1: Sync items
            launch(Dispatchers.IO) {
                try { firestoreLostItemRepository.syncFromFirestore() }
                catch (e: Exception) { e.printStackTrace() }
            }.join()

            // Step 2: Sync tips + claims
            launch(Dispatchers.IO) {
                try {
                    val allItems = lostItemRepository.getAllItems().first()
                    allItems.filter { it.reportedBy == currentUserId }.forEach { item ->
                        firestoreTipRepository.syncTipsFromFirestore(item.id)
                        firestoreClaimRepository.syncClaimsFromFirestore(item.id)
                    }
                    allItems.filter { it.reportedBy != currentUserId }.forEach { item ->
                        firestoreClaimRepository.syncClaimsFromFirestore(item.id)
                        firestoreTipRepository.syncTipsFromFirestore(item.id)
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }.join()

            // Step 3: Build notifications from fresh Room data
            try {
                lostItemRepository.getAllItems().collect { allItems ->
                    val userId  = sessionManager.currentUserId ?: return@collect
                    val now     = System.currentTimeMillis()
                    val readIds = getReadIds()
                    val notifications = mutableListOf<NotificationItem>()

                    // ══════════════════════════════════════════════════════
                    // MY ITEMS — I am the reporter
                    // ══════════════════════════════════════════════════════
                    val myItems = allItems.filter { it.reportedBy == userId }

                    myItems.forEach { item ->
                        val daysOld = TimeUnit.MILLISECONDS.toDays(now - item.reportedAt)

                        // 1. Item marked as FOUND
                        // ✅ FIXED: removed — reporter marked it themselves,
                        //    they don't need a notification about their own action.
                        //    The approved claimer (Mark) gets ITEM_RETURNED instead.

                        // 2. Item still pending 7+ days
                        if (item.status == ItemStatus.LOST && daysOld >= 7) {
                            notifications.add(NotificationItem(
                                id        = "${item.id}_pending",
                                type      = NotificationType.STILL_PENDING,
                                title     = "Still Looking?",
                                message   = "\"${item.title}\" has been lost for $daysOld days. Consider updating the description.",
                                timestamp = item.reportedAt,
                                itemId    = item.id
                            ))
                        }

                        // 3. Claims on my item
                        // ✅ FIXED: only show PENDING claims — skip rejected ones
                        //    so lhester isn't spammed with old rejected claim notifications
                        val claimsOnMyItem = claimRepository.getClaimsByItem(item.id).first()
                        claimsOnMyItem
                            .filter { it.status == ClaimStatus.PENDING }
                            .forEach { claim ->
                                notifications.add(NotificationItem(
                                    id        = "${claim.id}_claim_received",
                                    type      = NotificationType.CLAIM_RECEIVED,
                                    title     = "New Claim on Your Item ✋",
                                    message   = "${claim.claimerName} claims to have found \"${item.title}\".",
                                    timestamp = claim.claimedAt,
                                    itemId    = item.id
                                ))
                            }

                        // 4. Replies on claims of my item (from claimer)
                        claimsOnMyItem.forEach { claim ->
                            val replies = claimRepository.getRepliesByClaimId(claim.id).first()
                            replies.filter { it.authorId != userId }.forEach { reply ->
                                notifications.add(NotificationItem(
                                    id        = "${reply.id}_claim_reply_received",
                                    type      = NotificationType.CLAIM_REPLY_RECEIVED,
                                    title     = "New Reply on a Claim 💬",
                                    message   = "${reply.authorName} replied on the claim for \"${item.title}\": \"${reply.message.take(60)}${if (reply.message.length > 60) "..." else ""}\"",
                                    timestamp = reply.createdAt,
                                    itemId    = item.id
                                ))
                            }
                        }

                        // 5. Tips left on my item
                        val tips = tipRepository.getTipsByItemId(item.id).first()
                        tips.filter { it.authorId != userId && it.parentTipId == null }
                            .forEach { tip ->
                                notifications.add(NotificationItem(
                                    id        = "${tip.id}_tip_received",
                                    type      = NotificationType.TIP_RECEIVED,
                                    title     = "New Tip on Your Item 💡",
                                    message   = "Someone left a tip on \"${item.title}\": \"${tip.message.take(60)}${if (tip.message.length > 60) "..." else ""}\"",
                                    timestamp = tip.createdAt,
                                    itemId    = item.id
                                ))
                            }

                        // 6. Replies to tips on my item
                        tips.filter { it.authorId != userId && it.parentTipId != null }
                            .forEach { reply ->
                                notifications.add(NotificationItem(
                                    id        = "${reply.id}_tip_reply_on_my_item",
                                    type      = NotificationType.TIP_REPLY_RECEIVED,
                                    title     = "New Reply in Tip Thread ↩️",
                                    message   = "Someone replied in a tip thread on \"${item.title}\".",
                                    timestamp = reply.createdAt,
                                    itemId    = item.id
                                ))
                            }
                    }

                    // ══════════════════════════════════════════════════════
                    // OTHER PEOPLE'S ITEMS — I am the claimer / tipper
                    // ══════════════════════════════════════════════════════
                    val otherItems = allItems.filter { it.reportedBy != userId }

                    otherItems.forEach { item ->
                        val hoursOld = TimeUnit.MILLISECONDS.toHours(now - item.reportedAt)

                        // 7. New reports from others (last 24h)
                        if (item.status == ItemStatus.LOST && hoursOld < 24) {
                            notifications.add(NotificationItem(
                                id        = "${item.id}_new_report",
                                type      = NotificationType.NEW_REPORT,
                                title     = "New Lost Item Report 📢",
                                message   = "Someone reported \"${item.title}\" as lost on campus.",
                                timestamp = item.reportedAt,
                                itemId    = item.id
                            ))
                        }

                        // 8. My claims on other items
                        val allClaims = claimRepository.getClaimsByItem(item.id).first()
                        val myClaims  = allClaims.filter { it.claimerId == userId }

                        myClaims.forEach { claim ->

                            // Claim approved
                            if (claim.status == ClaimStatus.APPROVED) {
                                notifications.add(NotificationItem(
                                    id        = "${claim.id}_claim_approved",
                                    type      = NotificationType.CLAIM_APPROVED,
                                    title     = "Claim Approved! 🎉",
                                    message   = "Your claim for \"${item.title}\" was approved. You can now contact the owner.",
                                    timestamp = claim.claimedAt,
                                    itemId    = item.id
                                ))
                            }

                            // Claim rejected
                            if (claim.status == ClaimStatus.REJECTED) {
                                notifications.add(NotificationItem(
                                    id        = "${claim.id}_claim_rejected",
                                    type      = NotificationType.CLAIM_REJECTED,
                                    title     = "Claim Not Approved",
                                    message   = "Your claim for \"${item.title}\" was not approved by the owner.",
                                    timestamp = claim.claimedAt,
                                    itemId    = item.id
                                ))
                            }

                            // ✅ NEW: Notify approved claimer (Mark) when item is marked FOUND
                            // This confirms the item was successfully returned
                            if (claim.status == ClaimStatus.APPROVED &&
                                item.status == ItemStatus.FOUND) {
                                notifications.add(NotificationItem(
                                    id        = "${item.id}_${claim.id}_returned",
                                    type      = NotificationType.ITEM_RETURNED,
                                    title     = "Item Successfully Returned 📦",
                                    message   = "\"${item.title}\" has been marked as found. Thank you for helping!",
                                    timestamp = item.lastModifiedAt,
                                    itemId    = item.id
                                ))
                            }

                            // 9. Owner replied to MY claim
                            val replies = claimRepository.getRepliesByClaimId(claim.id).first()
                            replies.filter { it.authorId != userId }.forEach { reply ->
                                notifications.add(NotificationItem(
                                    id        = "${reply.id}_claim_reply_to_me",
                                    type      = NotificationType.CLAIM_REPLY_TO_YOU,
                                    title     = "Owner Replied to Your Claim 💬",
                                    message   = "${reply.authorName} replied to your claim on \"${item.title}\": \"${reply.message.take(60)}${if (reply.message.length > 60) "..." else ""}\"",
                                    timestamp = reply.createdAt,
                                    itemId    = item.id
                                ))
                            }
                        }

                        // 10. Replies to tips I posted
                        val allTips = tipRepository.getTipsByItemId(item.id).first()
                        val myTips  = allTips.filter {
                            it.authorId == userId && it.parentTipId == null
                        }
                        myTips.forEach { myTip ->
                            allTips.filter {
                                it.parentTipId == myTip.id && it.authorId != userId
                            }.forEach { reply ->
                                notifications.add(NotificationItem(
                                    id        = "${reply.id}_tip_reply_to_me",
                                    type      = NotificationType.TIP_REPLY_RECEIVED,
                                    title     = "Someone Replied to Your Tip ↩️",
                                    message   = "Your tip on \"${item.title}\" got a reply: \"${reply.message.take(60)}${if (reply.message.length > 60) "..." else ""}\"",
                                    timestamp = reply.createdAt,
                                    itemId    = item.id
                                ))
                            }
                        }
                    }

                    val sorted = notifications
                        .distinctBy { it.id }
                        .map { if (readIds.contains(it.id)) it.copy(isRead = true) else it }
                        .sortedByDescending { it.timestamp }
                    val unread = sorted.count { !it.isRead }

                    if (isFirstLoad) {
                        systemNotifiedIds.addAll(sorted.map { it.id })
                        isFirstLoad = false
                    } else {
                        sorted
                            .filter { !it.isRead && !systemNotifiedIds.contains(it.id) }
                            .forEach { notif ->
                                appNotificationManager.post(notif.id, notif.title, notif.message)
                                systemNotifiedIds.add(notif.id)
                            }
                    }

                    _uiState.update {
                        it.copy(
                            notifications = sorted,
                            unreadCount   = unread,
                            isLoading     = false,
                            error         = null
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load notifications")
                }
            }
        }
    }

    fun markAllAsRead() {
        val allIds = _uiState.value.notifications.map { it.id }.toSet()
        saveAllReadIds(allIds)
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { it.copy(isRead = true) },
                unreadCount   = 0
            )
        }
    }

    fun markAsRead(notificationId: String) {
        saveReadId(notificationId)
        _uiState.update { state ->
            val updated = state.notifications.map { notif ->
                if (notif.id == notificationId) notif.copy(isRead = true) else notif
            }
            state.copy(notifications = updated, unreadCount = updated.count { !it.isRead })
        }
    }
}