package com.campusfind.ui.screens.smarthistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.repository.LostItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SmartHistoryViewModel @Inject constructor(
    private val repository: LostItemRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartHistoryUiState())
    val uiState: StateFlow<SmartHistoryUiState> = _uiState.asStateFlow()

    init { loadHistory() }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.getAllItems()
                    .map { all -> all.filter { it.reportedBy == sessionManager.currentUserId } }
                    .collect { userItems ->
                        val activities = generateActivities(userItems)
                        val insights   = calculateInsights(userItems)
                        val filtered   = applyFilters(
                            activities,
                            _uiState.value.selectedFilter,
                            _uiState.value.selectedDateRange,
                            _uiState.value.customDate
                        )
                        _uiState.update {
                            it.copy(
                                activities            = activities,
                                filteredActivities    = filtered,
                                totalItemsPosted      = userItems.size,
                                itemsFound            = userItems.count { i -> i.status == ItemStatus.FOUND },
                                itemsStillLost        = userItems.count { i -> i.status == ItemStatus.LOST },
                                averageResolutionDays = insights.averageResolutionDays,
                                mostActiveDay         = insights.mostActiveDay,
                                successRate           = insights.successRate,
                                recentPosts           = userItems.count { i ->
                                    (System.currentTimeMillis() - i.reportedAt) < (7 * 24 * 60 * 60 * 1000L)
                                },
                                pendingItems          = userItems.count { i -> i.status == ItemStatus.LOST },
                                resolvedItems         = userItems.count { i -> i.status == ItemStatus.FOUND },
                                isLoading             = false,
                                error                 = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load history") }
            }
        }
    }

    fun onFilterChanged(filter: HistoryFilter) {
        _uiState.update {
            it.copy(
                selectedFilter     = filter,
                filteredActivities = applyFilters(it.activities, filter, it.selectedDateRange, it.customDate)
            )
        }
    }

    fun onDateRangeChanged(range: DateRange) {
        _uiState.update {
            // If switching away from CUSTOM, clear the custom date
            val customDate = if (range == DateRange.CUSTOM) it.customDate else null
            it.copy(
                selectedDateRange  = range,
                customDate         = customDate,
                showDatePicker     = range == DateRange.CUSTOM,
                filteredActivities = applyFilters(it.activities, it.selectedFilter, range, customDate)
            )
        }
    }

    fun onCustomDateSelected(date: CustomDate) {
        _uiState.update {
            it.copy(
                customDate         = date,
                selectedDateRange  = DateRange.CUSTOM,
                showDatePicker     = false,
                filteredActivities = applyFilters(it.activities, it.selectedFilter, DateRange.CUSTOM, date)
            )
        }
    }

    fun onDatePickerDismissed() {
        _uiState.update { state ->
            // If no custom date was ever set, revert to ALL_TIME
            if (state.customDate == null) {
                state.copy(showDatePicker = false, selectedDateRange = DateRange.ALL_TIME)
            } else {
                state.copy(showDatePicker = false)
            }
        }
    }

    fun showDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private fun applyFilters(
        activities: List<ActivityEvent>,
        filter: HistoryFilter,
        dateRange: DateRange,
        customDate: CustomDate?
    ): List<ActivityEvent> {
        var filtered = when (filter) {
            HistoryFilter.ALL     -> activities
            HistoryFilter.POSTED  -> activities.filter { it.type == ActivityType.ITEM_POSTED }
            HistoryFilter.FOUND   -> activities.filter { it.type == ActivityType.ITEM_FOUND }
            HistoryFilter.PENDING -> activities.filter {
                it.type == ActivityType.ITEM_POSTED && it.item?.status == ItemStatus.LOST
            }
        }

        filtered = when {
            dateRange == DateRange.CUSTOM && customDate != null -> {
                val start = customDate.toStartOfDayMillis()
                val end   = customDate.toEndOfDayMillis()
                filtered.filter { it.timestamp in start..end }
            }
            else -> {
                val threshold = dateRange.getTimestampThreshold()
                filtered.filter { it.timestamp >= threshold }
            }
        }

        return filtered
    }

    private fun generateActivities(
        items: List<com.campusfind.domain.model.LostItem>
    ): List<ActivityEvent> {
        val events = mutableListOf<ActivityEvent>()
        items.forEach { item ->
            events.add(ActivityEvent(
                id           = "${item.id}_posted",
                type         = ActivityType.ITEM_POSTED,
                title        = "Posted \"${item.title}\"",
                description  = item.description,
                timestamp    = item.reportedAt,
                itemId       = item.id,
                item         = item,
                insightBadge = if (item.status == ItemStatus.FOUND) {
                    val days = TimeUnit.MILLISECONDS.toDays(item.lastModifiedAt - item.reportedAt)
                    when { days == 0L -> "SAME DAY ⚡"; days <= 1L -> "QUICK FIND 🎯"; days <= 3L -> "RESOLVED ✓"; else -> null }
                } else {
                    val days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - item.reportedAt)
                    when { days >= 7L -> "NEEDS ATTENTION ⚠️"; days >= 3L -> "STILL PENDING"; else -> null }
                }
            ))
            if (item.status == ItemStatus.FOUND && item.lastModifiedAt > item.reportedAt) {
                events.add(ActivityEvent(
                    id          = "${item.id}_found",
                    type        = ActivityType.ITEM_FOUND,
                    title       = "Marked \"${item.title}\" as Found",
                    description = "Successfully recovered!",
                    timestamp   = item.lastModifiedAt,
                    itemId      = item.id,
                    item        = item
                ))
            }
        }
        return events.sortedByDescending { it.timestamp }
    }

    private fun calculateInsights(
        items: List<com.campusfind.domain.model.LostItem>
    ): SmartInsights {
        val resolved = items.filter { it.status == ItemStatus.FOUND && it.lastModifiedAt > it.reportedAt }
        val avgDays  = if (resolved.isNotEmpty())
            (resolved.sumOf { TimeUnit.MILLISECONDS.toDays(it.lastModifiedAt - it.reportedAt) } / resolved.size).toInt()
        else 0

        val mostActiveDay = items
            .groupBy { item ->
                SimpleDateFormat("EEEE", Locale.getDefault())
                    .format(Calendar.getInstance().apply { timeInMillis = item.reportedAt }.time)
            }
            .maxByOrNull { it.value.size }?.key

        val successRate = if (items.isNotEmpty())
            ((items.count { it.status == ItemStatus.FOUND }.toFloat() / items.size) * 100).toInt()
        else 0

        return SmartInsights(avgDays, mostActiveDay, successRate)
    }

    private data class SmartInsights(
        val averageResolutionDays: Int,
        val mostActiveDay: String?,
        val successRate: Int
    )
}