package com.campusfind.ui.screens.additem

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
class AddItemViewModel @Inject constructor(
    private val repository: LostItemRepository,
    private val sessionManager: SessionManager,
    private val photoManager: PhotoManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddItemUiState())
    val uiState: StateFlow<AddItemUiState> = _uiState.asStateFlow()

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    // ✅ NEW: Handle location changes
    fun onLocationChanged(location: String) {
        _uiState.update { it.copy(location = location) }
    }

    fun onPhotoSelected(uri: Uri?) {
        _uiState.update { it.copy(photoUri = uri) }
    }

    fun onRemovePhoto() {
        _uiState.update { it.copy(photoUri = null) }
    }

    fun onSubmit(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        // Validation
        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(error = "Title is required") }
            return
        }

        if (currentState.description.isBlank()) {
            _uiState.update { it.copy(error = "Description is required") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            try {
                // Save photo to internal storage (offline-first)
                val internalPhotoPath = currentState.photoUri?.let { uri ->
                    photoManager.savePhoto(uri)
                }

                // ✅ UPDATED: Now includes location
                val result = repository.addItem(
                    title = currentState.title.trim(),
                    description = currentState.description.trim(),
                    location = currentState.location.trim().ifBlank { null },  // ✅ NEW
                    photoUri = internalPhotoPath
                )

                if (result.isSuccess) {
                    _uiState.update { AddItemUiState() } // Reset form
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = result.exceptionOrNull()?.message ?: "Failed to save item"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = "Failed to save item: ${e.message}"
                    )
                }
            }
        }
    }
}