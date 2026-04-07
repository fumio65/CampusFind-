package com.campusfind.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.repository.FirestoreLostItemRepositoryImpl
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.repository.LostItemRepository
import com.campusfind.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LostItemRepository,
    private val userRepository: UserRepository,
    private val firestoreRepository: FirestoreLostItemRepositoryImpl  // ← for sync
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow<ItemStatus?>(null)
    private val _refreshKey = MutableStateFlow(0)

    init {
        observeItems()
    }

    private fun observeItems() {
        viewModelScope.launch {
            combine(_selectedFilter, _refreshKey) { filter, _ -> filter }
                .flatMapLatest { filter ->
                    _uiState.update { state ->
                        state.copy(isLoading = state.items.isEmpty())
                    }
                    if (filter == null) repository.getAllItems()
                    else repository.getItemsByStatus(filter)
                }
                .catch { exception ->
                    _uiState.update {
                        it.copy(isLoading = false,
                            error = exception.message ?: "Failed to load items")
                    }
                }
                .collect { items ->
                    val reporterNames = loadReporterNames(items.map { it.reportedBy }.distinct())
                    _uiState.update {
                        it.copy(items = items, reporterNames = reporterNames,
                            isLoading = false, error = null)
                    }
                }
        }
    }

    private suspend fun loadReporterNames(userIds: List<String>): Map<String, String> {
        val names = mutableMapOf<String, String>()
        userIds.forEach { userId ->
            val user = userRepository.getUserById(userId)
            names[userId] = user?.fullName ?: "Unknown User"
        }
        return names
    }

    fun onFilterChanged(filter: ItemStatus?) {
        _selectedFilter.value = filter
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun refresh() {
        // Sync from Firestore first to get latest photoUri values,
        // then increment refreshKey to re-read from Room
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestoreRepository.syncFromFirestore()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _refreshKey.value++
            }
        }
    }
}