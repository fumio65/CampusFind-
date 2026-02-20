package com.campusfind.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.repository.LostItemRepository
import com.campusfind.domain.repository.UserRepository
import com.campusfind.domain.usecase.DeleteItemUseCase
import com.campusfind.domain.usecase.UpdateItemStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/detail/DetailViewModel.kt
 *
 * ViewModel for the detail screen.
 *
 * UPDATED: Changed onMarkAsFound() to onToggleStatus() to support bidirectional toggle.
 *
 * Toggle behavior:
 * - If status = LOST → update to FOUND
 * - If status = FOUND → update back to LOST
 * - No confirmation needed (user can freely toggle)
 *
 * See: DEC-001 (MVVM), DEC-021 (ownership enforcement), TASK-114
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val lostItemRepository: LostItemRepository,
    private val userRepository: UserRepository,
    private val updateItemStatusUseCase: UpdateItemStatusUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    /**
     * Load item details and compute ownership.
     *
     * Called by: DetailScreen in LaunchedEffect
     */
    fun loadItem(itemId: String) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val item = lostItemRepository.getItemById(itemId)
                if (item == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Item not found or was deleted"
                        )
                    }
                    return@launch
                }

                // Compute ownership
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
     * Toggle item status between LOST and FOUND.
     *
     * NEW: Replaces onMarkAsFound() with bidirectional toggle.
     *
     * Called by: Toggle button in DetailScreen
     * Demo behavior:
     * - User marks item as Found → can undo by marking as Lost again
     * - No confirmation needed, user has full control
     */
    fun onToggleStatus() {
        val item = _uiState.value.item ?: return

        // Determine new status (opposite of current)
        val newStatus = when (item.status) {
            ItemStatus.LOST -> ItemStatus.FOUND
            ItemStatus.FOUND -> ItemStatus.LOST
        }

        viewModelScope.launch {
            // UpdateItemStatusUseCase now accepts a target status parameter
            val result = updateItemStatusUseCase(item, newStatus)
            result.fold(
                onSuccess = {
                    // Reload item to refresh status
                    loadItem(item.id)
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to update status")
                    }
                }
            )
        }
    }

    /**
     * Delete item after confirmation.
     *
     * Called by: Delete confirmation dialog
     *
     * @param onSuccess Navigate back to HomeScreen after successful deletion
     */
    fun onDeleteConfirmed(onSuccess: () -> Unit) {
        val item = _uiState.value.item ?: return

        viewModelScope.launch {
            val result = deleteItemUseCase(item)
            result.fold(
                onSuccess = {
                    // Navigate back — item is gone from database
                    onSuccess()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(error = exception.message ?: "Failed to delete item")
                    }
                }
            )
        }
    }
}