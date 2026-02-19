package com.campusfind.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.repository.LostItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/home/HomeViewModel.kt
 *
 * ViewModel for the home screen (item list with filters).
 *
 * Why @OptIn(ExperimentalCoroutinesApi::class):
 * - flatMapLatest is still marked as experimental in Kotlin coroutines
 * - @OptIn tells the compiler we're aware and accept the API
 * - This API is stable in practice and widely used
 *
 * See: DEC-001 (MVVM), DEC-020 (multi-user), DEC-022 (Hilt DI),
 *      TASK-111 (LostItemRepository), TASK-112, demo steps 5 & 9
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LostItemRepository
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
     * When selectedFilter changes:
     * - flatMapLatest cancels the old Flow
     * - Calls repository.getAllItems() or repository.getItemsByStatus()
     * - Collects the new Flow and updates UI state
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
                    _uiState.update {
                        it.copy(
                            items = items,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
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