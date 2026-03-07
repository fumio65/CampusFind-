package com.campusfind.ui.screens.edititem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.repository.LostItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditItemViewModel @Inject constructor(
    private val repository: LostItemRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditItemUiState())
    val uiState: StateFlow<EditItemUiState> = _uiState.asStateFlow()

    private var currentItemId: String? = null

    /**
     * Load existing item data
     */
    fun loadItem(itemId: String) {
        currentItemId = itemId
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
                if (item.reportedBy != sessionManager.currentUserId) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "You can only edit your own reports"
                        )
                    }
                    return@launch
                }

                // Pre-fill form with existing data
                _uiState.update {
                    it.copy(
                        title = item.title,
                        description = item.description,
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

    fun onTitleChanged(title: String) {
        _uiState.update {
            it.copy(
                title = title,
                titleError = null
            )
        }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update {
            it.copy(
                description = description,
                descriptionError = null
            )
        }
    }

    fun onSave(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val itemId = currentItemId

        // Validation
        var hasError = false

        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            hasError = true
        }

        if (currentState.description.isBlank()) {
            _uiState.update { it.copy(descriptionError = "Description is required") }
            hasError = true
        }

        if (hasError || itemId == null) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                // Get the existing item
                val existingItem = repository.getItemById(itemId)

                if (existingItem == null) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = "Item not found"
                        )
                    }
                    return@launch
                }

                // Update title and description
                repository.updateItemDetails(
                    id = itemId,
                    title = currentState.title.trim(),
                    description = currentState.description.trim()
                )

                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to save: ${e.message}"
                    )
                }
            }
        }
    }
}

data class EditItemUiState(
    val title: String = "",
    val description: String = "",
    val titleError: String? = null,
    val descriptionError: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)