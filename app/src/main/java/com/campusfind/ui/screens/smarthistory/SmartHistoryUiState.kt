package com.campusfind.ui.screens.smarthistory

import com.campusfind.domain.model.LostItem

data class SmartHistoryUiState(
    val activities: List<ActivityEvent> = emptyList(),
    val filteredActivities: List<ActivityEvent> = emptyList(),

    val totalItemsPosted: Int = 0,
    val itemsFound: Int = 0,
    val itemsStillLost: Int = 0,
    val averageResolutionDays: Int = 0,
    val mostActiveDay: String? = null,
    val successRate: Int = 0,

    val recentPosts: Int = 0,
    val pendingItems: Int = 0,
    val resolvedItems: Int = 0,

    val selectedFilter: HistoryFilter = HistoryFilter.ALL,
    val selectedDateRange: DateRange = DateRange.ALL_TIME,

    // Custom date picker state
    val customDate: CustomDate? = null,          // non-null = custom date is active
    val showDatePicker: Boolean = false,         // controls bottom sheet visibility

    val isLoading: Boolean = false,
    val error: String? = null
)

// ── Custom date (exact year + month + day) ─────────────────────────────────

data class CustomDate(
    val year: Int,
    val month: Int,   // 1–12
    val day: Int      // 1–31
) {
    fun getLabel(): String {
        val monthName = java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault())
            .format(java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.MONTH, month - 1)
            }.time)
        return "$monthName $day, $year"
    }

    fun toStartOfDayMillis(): Long {
        return java.util.Calendar.getInstance().apply {
            set(year, month - 1, day, 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun toEndOfDayMillis(): Long {
        return java.util.Calendar.getInstance().apply {
            set(year, month - 1, day, 23, 59, 59)
            set(java.util.Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
}

// ── Activity event ─────────────────────────────────────────────────────────

data class ActivityEvent(
    val id: String,
    val type: ActivityType,
    val title: String,
    val description: String,
    val timestamp: Long,
    val itemId: String? = null,
    val item: LostItem? = null,
    val insightBadge: String? = null
)

// ── Activity type ──────────────────────────────────────────────────────────

enum class ActivityType {
    ITEM_POSTED,
    ITEM_FOUND,
    ITEM_EDITED,
    ITEM_DELETED,
    CLAIM_RECEIVED,
    TIP_RECEIVED,
    ITEM_VIEWED;

    fun getIcon(): String = when (this) {
        ITEM_POSTED    -> "📢"
        ITEM_FOUND     -> "✅"
        ITEM_EDITED    -> "✏️"
        ITEM_DELETED   -> "🗑️"
        CLAIM_RECEIVED -> "✋"
        TIP_RECEIVED   -> "💬"
        ITEM_VIEWED    -> "👁️"
    }

    fun getColor(): androidx.compose.ui.graphics.Color = when (this) {
        ITEM_POSTED    -> androidx.compose.ui.graphics.Color(0xFF6C63FF)
        ITEM_FOUND     -> androidx.compose.ui.graphics.Color(0xFF2DD4A0)
        ITEM_EDITED    -> androidx.compose.ui.graphics.Color(0xFFFFA726)
        ITEM_DELETED   -> androidx.compose.ui.graphics.Color(0xFFFF4D6D)
        CLAIM_RECEIVED -> androidx.compose.ui.graphics.Color(0xFF42A5F5)
        TIP_RECEIVED   -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
        ITEM_VIEWED    -> androidx.compose.ui.graphics.Color(0xFF78909C)
    }
}

// ── History filter ─────────────────────────────────────────────────────────

enum class HistoryFilter {
    ALL, POSTED, FOUND, PENDING;

    fun getLabel(): String = when (this) {
        ALL     -> "All Activity"
        POSTED  -> "Posted"
        FOUND   -> "Resolved"
        PENDING -> "Pending"
    }
}

// ── Date range filter ──────────────────────────────────────────────────────

enum class DateRange {
    ALL_TIME, LAST_7_DAYS, LAST_30_DAYS, LAST_90_DAYS, CUSTOM;

    fun getLabel(): String = when (this) {
        ALL_TIME      -> "All Time"
        LAST_7_DAYS   -> "Last 7 Days"
        LAST_30_DAYS  -> "Last 30 Days"
        LAST_90_DAYS  -> "Last 90 Days"
        CUSTOM        -> "Custom"
    }

    fun getShortLabel(): String = when (this) {
        ALL_TIME      -> "All"
        LAST_7_DAYS   -> "7d"
        LAST_30_DAYS  -> "30d"
        LAST_90_DAYS  -> "90d"
        CUSTOM        -> ""   // not used — icon is shown instead in DateRangeSegmented
    }

    fun getTimestampThreshold(): Long {
        val now = System.currentTimeMillis()
        return when (this) {
            ALL_TIME      -> 0L
            LAST_7_DAYS   -> now - (7L  * 24 * 60 * 60 * 1000)
            LAST_30_DAYS  -> now - (30L * 24 * 60 * 60 * 1000)
            LAST_90_DAYS  -> now - (90L * 24 * 60 * 60 * 1000)
            CUSTOM        -> 0L  // handled separately via CustomDate
        }
    }
}