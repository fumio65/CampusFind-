package com.campusfind.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.model.ClaimStatus
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.repository.ClaimRepository
import com.campusfind.domain.repository.LostItemRepository
import com.campusfind.domain.repository.TipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import android.content.SharedPreferences
import javax.inject.Inject

// ── Notification types ─────────────────────────────────────────────────────

enum class NotificationType {
    // Item activity
    ITEM_FOUND,           // your item was marked as found
    STILL_PENDING,        // your item pending 7+ days
    NEW_REPORT,           // new item reported on campus (last 24h)

    // Claim activity
    CLAIM_RECEIVED,       // someone submitted a claim on YOUR item
    CLAIM_APPROVED,       // your submitted claim was approved
    CLAIM_REJECTED,       // your submitted claim was rejected

    // Reply activity
    CLAIM_REPLY_RECEIVED, // someone replied to a claim on YOUR item
    CLAIM_REPLY_TO_YOU,   // owner replied to YOUR claim

    // Tip activity
    TIP_RECEIVED,         // someone left a tip on YOUR item
    TIP_REPLY_RECEIVED;   // someone replied to YOUR tip

    fun getIcon(): String = when (this) {
        ITEM_FOUND             -> "✅"
        STILL_PENDING          -> "⏳"
        NEW_REPORT             -> "📢"
        CLAIM_RECEIVED         -> "✋"
        CLAIM_APPROVED         -> "🎉"
        CLAIM_REJECTED         -> "❌"
        CLAIM_REPLY_RECEIVED   -> "💬"
        CLAIM_REPLY_TO_YOU     -> "💬"
        TIP_RECEIVED           -> "💡"
        TIP_REPLY_RECEIVED     -> "↩️"
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

// ── ViewModel ──────────────────────────────────────────────────────────────

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val lostItemRepository: LostItemRepository,
    private val claimRepository: ClaimRepository,
    private val tipRepository: TipRepository,
    private val sessionManager: SessionManager,
    private val prefs: SharedPreferences
) : ViewModel() {

    // Persisted set of notification IDs the user has read.
    // Key is USER-SPECIFIC — cached at init so it never changes mid-session.
    // This prevents the "guest" fallback from mixing with real user read state.
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
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            // Guard — don't load if no user is logged in
            val currentUserId = sessionManager.currentUserId
            if (currentUserId == null) {
                _uiState.update { it.copy(isLoading = false, notifications = emptyList(), unreadCount = 0) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            try {
                lostItemRepository.getAllItems().collect { allItems ->
                    val currentUserId = sessionManager.currentUserId ?: return@collect
                    val now = System.currentTimeMillis()
                    val readIds = getReadIds()
                    val notifications = mutableListOf<NotificationItem>()

                    // ══════════════════════════════════════════════════════
                    // MY ITEMS — I am the reporter
                    // ══════════════════════════════════════════════════════
                    val myItems = allItems.filter { it.reportedBy == currentUserId }

                    myItems.forEach { item ->
                        val daysOld = TimeUnit.MILLISECONDS.toDays(now - item.reportedAt)
                        val daysModified = TimeUnit.MILLISECONDS.toDays(now - item.lastModifiedAt)

                        // ── 1. Item marked as found ────────────────────────
                        if (item.status == ItemStatus.FOUND &&
                            item.lastModifiedAt > item.reportedAt) {
                            notifications.add(NotificationItem(
                                id        = "${item.id}_found",
                                type      = NotificationType.ITEM_FOUND,
                                title     = "Item Recovered! 🎉",
                                message   = "\"${item.title}\" has been marked as found.",
                                timestamp = item.lastModifiedAt,
                                itemId    = item.id,
                                isRead    = false
                            ))
                        }

                        // ── 2. Item still pending 7+ days ─────────────────
                        if (item.status == ItemStatus.LOST && daysOld >= 7) {
                            notifications.add(NotificationItem(
                                id        = "${item.id}_pending",
                                type      = NotificationType.STILL_PENDING,
                                title     = "Still Looking?",
                                message   = "\"${item.title}\" has been lost for $daysOld days. Consider updating the description.",
                                timestamp = item.reportedAt,
                                itemId    = item.id,
                                isRead    = false
                            ))
                        }

                        // ── 3. Claims submitted on my item ─────────────────
                        val claimsOnMyItem = claimRepository.getClaimsByItem(item.id).first()
                        claimsOnMyItem.forEach { claim ->
                            val claimAgeHours = TimeUnit.MILLISECONDS
                                .toHours(now - claim.claimedAt)

                            // Someone submitted a claim on my item
                            notifications.add(NotificationItem(
                                id        = "${claim.id}_claim_received",
                                type      = NotificationType.CLAIM_RECEIVED,
                                title     = "New Claim on Your Item ✋",
                                message   = "${claim.claimerName} claims to have found \"${item.title}\".",
                                timestamp = claim.claimedAt,
                                itemId    = item.id,
                                isRead    = false
                            ))

                            // ── 4. Replies made on claims of my item ───────
                            // i.e. the claimer replied back after I replied
                            val replies = claimRepository.getRepliesByClaimId(claim.id).first()
                            // Replies NOT authored by me on MY item's claims
                            replies.filter { it.authorId != currentUserId }
                                .forEach { reply ->
                                    val replyAgeHours = TimeUnit.MILLISECONDS
                                        .toHours(now - reply.createdAt)
                                    notifications.add(NotificationItem(
                                        id        = "${reply.id}_claim_reply_received",
                                        type      = NotificationType.CLAIM_REPLY_RECEIVED,
                                        title     = "New Reply on a Claim 💬",
                                        message   = "${reply.authorName} replied on the claim for \"${item.title}\": \"${reply.message.take(60)}${if (reply.message.length > 60) "..." else ""}\"",
                                        timestamp = reply.createdAt,
                                        itemId    = item.id,
                                        isRead    = false
                                    ))
                                }
                        }

                        // ── 5. Tips left on my item ────────────────────────
                        val tipCount = tipRepository.getTipCount(item.id)
                        if (tipCount > 0) {
                            val tips = tipRepository.getTipsByItemId(item.id).first()
                            // Only top-level tips (not replies) from OTHER users
                            tips.filter { it.authorId != currentUserId && it.parentTipId == null }
                                .forEach { tip ->
                                    val tipAgeHours = TimeUnit.MILLISECONDS
                                        .toHours(now - tip.createdAt)
                                    notifications.add(NotificationItem(
                                        id        = "${tip.id}_tip_received",
                                        type      = NotificationType.TIP_RECEIVED,
                                        title     = "New Tip on Your Item 💡",
                                        message   = "Someone left a tip on \"${item.title}\": \"${tip.message.take(60)}${if (tip.message.length > 60) "..." else ""}\"",
                                        timestamp = tip.createdAt,
                                        itemId    = item.id,
                                        isRead    = false
                                    ))
                                }

                            // ── 6. Replies to tips on my item ──────────────
                            // Someone replied to a tip thread on my item
                            tips.filter { it.authorId != currentUserId && it.parentTipId != null }
                                .forEach { reply ->
                                    val replyAgeHours = TimeUnit.MILLISECONDS
                                        .toHours(now - reply.createdAt)
                                    notifications.add(NotificationItem(
                                        id        = "${reply.id}_tip_reply_on_my_item",
                                        type      = NotificationType.TIP_REPLY_RECEIVED,
                                        title     = "New Reply in Tip Thread ↩️",
                                        message   = "Someone replied in a tip thread on \"${item.title}\".",
                                        timestamp = reply.createdAt,
                                        itemId    = item.id,
                                        isRead    = false
                                    ))
                                }
                        }
                    }

                    // ══════════════════════════════════════════════════════
                    // OTHER PEOPLE'S ITEMS — I am the claimer / tipper
                    // ══════════════════════════════════════════════════════
                    val otherItems = allItems.filter { it.reportedBy != currentUserId }

                    otherItems.forEach { item ->
                        val hoursOld = TimeUnit.MILLISECONDS.toHours(now - item.reportedAt)

                        // ── 7. New reports from others (last 24h) ──────────
                        if (item.status == ItemStatus.LOST && hoursOld < 24) {
                            notifications.add(NotificationItem(
                                id        = "${item.id}_new_report",
                                type      = NotificationType.NEW_REPORT,
                                title     = "New Lost Item Report 📢",
                                message   = "Someone reported \"${item.title}\" as lost on campus.",
                                timestamp = item.reportedAt,
                                itemId    = item.id,
                                isRead    = false
                            ))
                        }

                        // ── 8. My claims on other items ────────────────────
                        val allClaims = claimRepository.getClaimsByItem(item.id).first()
                        val myClaims = allClaims.filter { it.claimerId == currentUserId }

                        myClaims.forEach { claim ->
                            val claimAgeHours = TimeUnit.MILLISECONDS
                                .toHours(now - claim.claimedAt)

                            // Claim approved
                            if (claim.status == ClaimStatus.APPROVED) {
                                notifications.add(NotificationItem(
                                    id        = "${claim.id}_claim_approved",
                                    type      = NotificationType.CLAIM_APPROVED,
                                    title     = "Claim Approved! 🎉",
                                    message   = "Your claim for \"${item.title}\" was approved. You can now contact the owner.",
                                    timestamp = claim.claimedAt,
                                    itemId    = item.id,
                                    isRead    = false
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
                                    itemId    = item.id,
                                    isRead    = false
                                ))
                            }

                            // ── 9. Owner replied to MY claim ───────────────
                            val replies = claimRepository.getRepliesByClaimId(claim.id).first()
                            // Replies NOT from me = owner or others replying to my claim
                            replies.filter { it.authorId != currentUserId }
                                .forEach { reply ->
                                    val replyAgeHours = TimeUnit.MILLISECONDS
                                        .toHours(now - reply.createdAt)
                                    notifications.add(NotificationItem(
                                        id        = "${reply.id}_claim_reply_to_me",
                                        type      = NotificationType.CLAIM_REPLY_TO_YOU,
                                        title     = "Owner Replied to Your Claim 💬",
                                        message   = "${reply.authorName} replied to your claim on \"${item.title}\": \"${reply.message.take(60)}${if (reply.message.length > 60) "..." else ""}\"",
                                        timestamp = reply.createdAt,
                                        itemId    = item.id,
                                        isRead    = false
                                    ))
                                }
                        }

                        // ── 10. Replies to tips I posted ───────────────────
                        val allTips = tipRepository.getTipsByItemId(item.id).first()
                        // My top-level tips
                        val myTips = allTips.filter { it.authorId == currentUserId && it.parentTipId == null }

                        myTips.forEach { myTip ->
                            // Replies to my tip from others
                            allTips.filter {
                                it.parentTipId == myTip.id && it.authorId != currentUserId
                            }.forEach { reply ->
                                val replyAgeHours = TimeUnit.MILLISECONDS
                                    .toHours(now - reply.createdAt)
                                notifications.add(NotificationItem(
                                    id        = "${reply.id}_tip_reply_to_me",
                                    type      = NotificationType.TIP_REPLY_RECEIVED,
                                    title     = "Someone Replied to Your Tip ↩️",
                                    message   = "Your tip on \"${item.title}\" got a reply: \"${reply.message.take(60)}${if (reply.message.length > 60) "..." else ""}\"",
                                    timestamp = reply.createdAt,
                                    itemId    = item.id,
                                    isRead    = false
                                ))
                            }
                        }
                    }

                    // Sort newest first, remove duplicates
                    // Re-apply persisted read state so tapped notifications stay read
                    val sorted = notifications
                        .distinctBy { it.id }
                        .map { if (readIds.contains(it.id)) it.copy(isRead = true) else it }
                        .sortedByDescending { it.timestamp }
                    val unread = sorted.count { !it.isRead }

                    _uiState.update {
                        it.copy(
                            notifications = sorted,
                            unreadCount   = unread,
                            isLoading     = false,
                            error         = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error     = e.message ?: "Failed to load notifications"
                    )
                }
            }
        }
    }

    fun markAllAsRead() {
        // Persist all current notification IDs as read
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
        // Persist this ID so it stays read after recomposition
        saveReadId(notificationId)

        _uiState.update { state ->
            val updated = state.notifications.map { notif ->
                if (notif.id == notificationId) notif.copy(isRead = true)
                else notif
            }
            state.copy(
                notifications = updated,
                unreadCount   = updated.count { !it.isRead }
            )
        }
    }
}