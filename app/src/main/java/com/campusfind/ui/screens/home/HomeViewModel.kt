package com.campusfind.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.repository.LostItemRepository
import com.campusfind.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/home/HomeViewModel.kt
 *
 * ViewModel for the home screen (item list with filters).
 *
 * UPDATED: Now loads reporter names for all items
 * - Injects UserRepository to fetch user details
 * - After items load, fetches all unique reporter names
 * - Stores in reporterNames Map (userId -> fullName)
 * - ModernItemCard displays actual names instead of "Reporter"
 *
 * Why @HiltViewModel:
 * - Hilt injects LostItemRepository + UserRepository automatically
 * - HomeScreen calls hiltViewModel() to get this instance
 *
 * Why flatMapLatest:
 * - When selectedFilter changes, cancel the old Flow and start a new one
 * - If user rapidly taps filter chips, only the latest query runs
 * - Prevents race conditions and unnecessary database queries
 *
 * Flow chain:
 * 1. User taps filter chip → onFilterChanged() updates _selectedFilter
 * 2. flatMapLatest cancels old Flow, calls repository with new filter
 * 3. Repository returns Flow<List<LostItem>> from Room
 * 4. For each batch of items, load all unique reporter names from UserRepository
 * 5. collect { } updates _uiState with items + reporterNames
 * 6. HomeScreen recomposes automatically via collectAsState()
 *
 * Why this satisfies demo step 9 (User B sees User A's items):
 * - getAllItems() calls repository.getAllItems() which calls dao.getAllItems()
 * - DAO query has no WHERE userId = ... clause
 * - All items from all users are returned (DEC-020 multi-user shared DB)
 * - User A's name is fetched and displayed on their item card
 *
 * See: DEC-001 (MVVM), DEC-020 (multi-user), DEC-022 (Hilt DI),
 *      TASK-111 (LostItemRepository), TASK-112, demo steps 5 & 9
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LostItemRepository,
    private val userRepository: UserRepository  // ← NEW: for loading reporter names
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow<ItemStatus?>(null)

    init {
        observeItems()
    }

    /**
     * Observe items from repository with reactive filtering.
     *
     * UPDATED: Now also loads reporter names after items are fetched
     *
     * When selectedFilter changes:
     * - flatMapLatest cancels the old Flow
     * - Calls repository.getAllItems() or repository.getItemsByStatus()
     * - Loads all unique reporter names from UserRepository
     * - Collects the new Flow and updates UI state with items + names
     */
    private fun observeItems() {
        viewModelScope.launch {
            _selectedFilter
                .flatMapLatest { filter ->
                    _uiState.update { it.copy(isLoading = true) }
                    if (filter == null) {
                        repository.getAllItems()
                    } else {
                        repository.getItemsByStatus(filter)
                    }
                }
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load items"
                        )
                    }
                }
                .collect { items ->
                    // Load reporter names for all unique user IDs
                    val reporterNames = loadReporterNames(items.map { it.reportedBy }.distinct())

                    _uiState.update {
                        it.copy(
                            items = items,
                            reporterNames = reporterNames,  // ← NEW
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    /**
     * Load reporter full names for a list of user IDs.
     *
     * @param userIds List of unique user IDs (reportedBy values)
     * @return Map of userId -> fullName (or "Unknown User" if not found)
     *
     * Called by: observeItems() after items are loaded
     */
    private suspend fun loadReporterNames(userIds: List<String>): Map<String, String> {
        val names = mutableMapOf<String, String>()

        userIds.forEach { userId ->
            val user = userRepository.getUserById(userId)
            names[userId] = user?.fullName ?: "Unknown User"
        }

        return names
    }

    /**
     * Handle filter chip selection.
     *
     * @param filter null = All, ItemStatus.LOST = Lost only, ItemStatus.FOUND = Found only
     *
     * Called by: FilterChips composable onClick
     */
    fun onFilterChanged(filter: ItemStatus?) {
        _selectedFilter.value = filter
        _uiState.update { it.copy(selectedFilter = filter) }
    }
}