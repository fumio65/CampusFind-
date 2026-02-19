package com.campusfind.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.preferences.SessionManager
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
 * Why @HiltViewModel:
 * - Hilt injects repositories, use cases, and SessionManager automatically
 * - DetailScreen calls hiltViewModel() to get this instance
 *
 * Why ownership is computed here (DEC-021):
 * - item.reportedBy == sessionManager.currentUserId
 * - Business logic lives in ViewModel, not in Composable
 * - UI receives isOwner boolean and renders accordingly
 * - Even if UI tries to call onMarkAsFound(), UseCase double-checks ownership
 *
 * Why UserRepository is injected:
 * - To load reporter's full name via getUserById()
 * - Displayed as "Reported by: [name]"
 *
 * See: DEC-001 (MVVM), DEC-021 (ownership in ViewModel), DEC-022 (Hilt DI),
 *      TASK-114, demo steps 10, 13, 14
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
     * Mark item as found.
     *
     * Called by: "Mark as Found" button (only visible to owner)
     * Demo step: 14 — User A marks their item as Found
     */
    fun onMarkAsFound() {
        val item = _uiState.value.item ?: return

        viewModelScope.launch {
            val result = updateItemStatusUseCase(item)
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