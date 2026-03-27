package com.campusfind.ui.screens.edititem

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.photo.PhotoManager
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.repository.LostItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditItemViewModel @Inject constructor(
    private val repository: LostItemRepository,
    private val sessionManager: SessionManager,
    private val photoManager: PhotoManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditItemUiState())
    val uiState: StateFlow<EditItemUiState> = _uiState.asStateFlow()

    private var itemId: String? = null

    fun loadItem(id: String) {
        itemId = id
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val item = repository.getItemById(id)

                if (item == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Item not found",
                            isItemLoaded = false
                        )
                    }
                    return@launch
                }

                // Check ownership
                val currentUserId = sessionManager.currentUserId
                if (item.reportedBy != currentUserId) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "You can only edit your own reports",
                            isItemLoaded = false
                        )
                    }
                    return@launch
                }

                // Pre-fill form with current values
                _uiState.update {
                    it.copy(
                        title = item.title,
                        description = item.description,
                        location = item.location ?: "",
                        currentPhotoUri = item.photoUri,
                        selectedPhotoUri = null,
                        isLoading = false,
                        isItemLoaded = true,
                        error = null
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load item: ${e.message}",
                        isItemLoaded = false
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

    fun onLocationChanged(location: String) {
        _uiState.update { it.copy(location = location) }
    }

    fun onPhotoSelected(uri: Uri?) {
        _uiState.update { it.copy(selectedPhotoUri = uri) }
    }

    fun onRemovePhoto() {
        _uiState.update {
            it.copy(
                selectedPhotoUri = null,
                currentPhotoUri = null
            )
        }
    }

    fun onSave(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val id = itemId ?: return

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

        if (hasError) return

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                // Handle photo:
                // 1. If new photo selected → save it and use new path
                // 2. If photo was removed (both null) → delete old photo, use null
                // 3. If no change (selectedPhotoUri null, currentPhotoUri exists) → keep current

                val finalPhotoPath: String? = when {
                    // New photo selected
                    currentState.selectedPhotoUri != null -> {
                        // Delete old photo if exists
                        if (currentState.currentPhotoUri != null) {
                            photoManager.deletePhoto(currentState.currentPhotoUri)
                        }
                        // Save new photo
                        photoManager.savePhoto(currentState.selectedPhotoUri)
                    }
                    // Photo was removed
                    currentState.currentPhotoUri == null && currentState.selectedPhotoUri == null -> {
                        null
                    }
                    // No change - keep current photo
                    else -> currentState.currentPhotoUri
                }

                // Update item in database
                val result = repository.updateItemDetails(
                    id = id,
                    title = currentState.title.trim(),
                    description = currentState.description.trim(),
                    location = currentState.location.trim().ifBlank { null }
                )

                if (result.isSuccess) {
                    _uiState.update { EditItemUiState() }
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = result.exceptionOrNull()?.message ?: "Failed to save changes"
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to save changes: ${e.message}"
                    )
                }
            }
        }
    }
}