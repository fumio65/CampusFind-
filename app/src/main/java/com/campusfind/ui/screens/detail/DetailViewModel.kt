package com.campusfind.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import com.campusfind.domain.repository.LostItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for DetailScreen
 * Handles item loading, status updates, and deletion
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: LostItemRepository,
    private val userRepository: com.campusfind.domain.repository.UserRepository,  // ← ADD THIS
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    /**
     * Load item by ID and determine ownership
     */
    fun loadItem(itemId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val item = repository.getItemById(itemId)

                if (item == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Item not found"
                        )
                    }
                    return@launch
                }

                // Check ownership
                val isOwner = item.reportedBy == sessionManager.currentUserId

                // Load reporter name
                val reporter = userRepository.getUserById(item.reportedBy)
                val reporterName = reporter?.fullName ?: "Unknown User"

                _uiState.update {
                    it.copy(
                        item = item,
                        reporterName = reporterName,
                        isOwner = isOwner,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load item"
                    )
                }
            }
        }
    }

    /**
     * Update item status (LOST ↔ FOUND)
     * Only callable by item owner
     */
    fun onStatusUpdated(itemId: String, newStatus: ItemStatus) {
        viewModelScope.launch {
            try {
                repository.updateStatus(itemId, newStatus)

                // Update local state immediately
                _uiState.update { state ->
                    state.copy(
                        item = state.item?.copy(
                            status = newStatus,
                            lastModifiedAt = System.currentTimeMillis()
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to update status: ${e.message}")
                }
            }
        }
    }

    /**
     * Delete item permanently
     * Only callable by item owner
     */
    fun onDeleteItem(itemId: String) {
        viewModelScope.launch {
            try {
                repository.deleteItem(itemId)
                // Screen will navigate back after this
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to delete item: ${e.message}")
                }
            }
        }
    }
}