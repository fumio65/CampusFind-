package com.campusfind.ui.screens.home

import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/home/HomeUiState.kt
 *
 * UI state for the home screen (item list).
 *
 * Why items is a List not a Flow:
 * - HomeViewModel exposes StateFlow<HomeUiState>
 * - The items list inside the state updates whenever Room emits a new list
 * - Compose collectAsState() automatically recomposes when items changes
 *
 * Why selectedFilter is nullable:
 * - null = All items (no filter)
 * - ItemStatus.LOST = only lost items
 * - ItemStatus.FOUND = only found items
 *
 * Why isLoading:
 * - Shows loading indicator on first load
 * - In practice, Room queries are instant so this is rarely visible
 * - Included for completeness and future network sync
 *
 * See: DEC-001 (MVVM), TASK-112, demo steps 5 & 9
 */
data class HomeUiState(
    val items: List<LostItem> = emptyList(),
    val selectedFilter: ItemStatus? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)