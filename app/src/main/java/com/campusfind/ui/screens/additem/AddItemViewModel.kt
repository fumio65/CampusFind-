package com.campusfind.ui.screens.additem

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.photo.PhotoManager
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.data.remote.FirebaseStorageManager
import com.campusfind.domain.repository.LostItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddItemViewModel @Inject constructor(
    private val repository: LostItemRepository,
    private val sessionManager: SessionManager,
    private val photoManager: PhotoManager,
    private val storageManager: FirebaseStorageManager   // ← NEW
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddItemUiState())
    val uiState: StateFlow<AddItemUiState> = _uiState.asStateFlow()

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

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
                val userId = sessionManager.currentUserId
                    ?: run {
                        _uiState.update { it.copy(isSubmitting = false, error = "Not logged in") }
                        return@launch
                    }

                val itemId = UUID.randomUUID().toString()

                // ── Photo handling ─────────────────────────────────────────
                // Strategy: save locally first (offline-first), then upload to
                // Firebase Storage for cross-device access
                val photoUriToSave: String? = currentState.photoUri?.let { uri ->

                    // 1. Save locally for immediate offline display
                    val localPath = photoManager.savePhoto(uri)

                    // 2. Upload to Firebase Storage — returns download URL
                    //    which works on any device
                    val downloadUrl = storageManager.uploadItemPhoto(
                        contentUri = uri,
                        userId     = userId,
                        itemId     = itemId
                    )

                    // Use download URL if upload succeeded,
                    // fall back to local path if offline
                    downloadUrl ?: localPath
                }

                val result = repository.addItem(
                    title       = currentState.title.trim(),
                    description = currentState.description.trim(),
                    location    = currentState.location.trim().ifBlank { null },
                    photoUri    = photoUriToSave   // Firebase Storage URL or local path
                )

                if (result.isSuccess) {
                    _uiState.update { AddItemUiState() }
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