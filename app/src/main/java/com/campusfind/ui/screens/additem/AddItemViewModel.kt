package com.campusfind.ui.screens.additem

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.photo.PhotoManager
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.data.remote.SupabaseStorageManager
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
    private val storageManager: SupabaseStorageManager
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

                // ── Photo handling ─────────────────────────────────────────
                // Strategy:
                // 1. Save locally first → immediate display on reporter's device
                // 2. Upload to Supabase BEFORE saving to Firestore
                //    → Firestore gets the HTTPS URL so other devices see the photo
                // 3. Save item with Supabase URL (or local path if offline)
                val localPath = currentState.photoUri?.let { uri ->
                    photoManager.savePhoto(uri)
                }

                // Upload to Supabase FIRST — so Firestore gets the public URL
                val supabaseUrl = if (localPath != null) {
                    storageManager.uploadPhoto(localPath).also { url ->
                        if (url != null)
                            android.util.Log.d("Supabase", "✅ Upload success before save: $url")
                        else
                            android.util.Log.w("Supabase", "⚠️ Upload failed, using local path")
                    }
                } else null

                // Use Supabase URL if available, fall back to local path
                val photoUriToSave = supabaseUrl ?: localPath

                val result = repository.addItem(
                    title       = currentState.title.trim(),
                    description = currentState.description.trim(),
                    location    = currentState.location.trim().ifBlank { null },
                    photoUri    = photoUriToSave  // HTTPS URL → Firestore → all devices see it ✅
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
