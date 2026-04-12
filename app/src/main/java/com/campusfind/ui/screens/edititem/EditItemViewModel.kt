package com.campusfind.ui.screens.edititem

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.photo.PhotoManager
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.repository.LostItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "EDIT_DEBUG"

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
        Log.d(TAG, "loadItem: id=$id")
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val item = repository.getItemById(id)

                if (item == null) {
                    Log.e(TAG, "loadItem: item not found")
                    _uiState.update {
                        it.copy(isLoading = false, error = "Item not found", isItemLoaded = false)
                    }
                    return@launch
                }

                val currentUserId = sessionManager.currentUserId
                if (item.reportedBy != currentUserId) {
                    Log.e(TAG, "loadItem: ownership check failed")
                    _uiState.update {
                        it.copy(isLoading = false,
                            error = "You can only edit your own reports",
                            isItemLoaded = false)
                    }
                    return@launch
                }

                Log.d(TAG, "loadItem: success, photoUri=${item.photoUri}")
                _uiState.update {
                    it.copy(
                        title            = item.title,
                        description      = item.description,
                        location         = item.location ?: "",
                        currentPhotoUri  = item.photoUri,
                        selectedPhotoUri = null,
                        photoRemoved     = false,
                        isLoading        = false,
                        isItemLoaded     = true,
                        error            = null
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "loadItem: exception ${e.message}")
                _uiState.update {
                    it.copy(isLoading = false,
                        error = "Failed to load item: ${e.message}",
                        isItemLoaded = false)
                }
            }
        }
    }

    fun onTitleChanged(title: String) =
        _uiState.update { it.copy(title = title, titleError = null) }

    fun onDescriptionChanged(description: String) =
        _uiState.update { it.copy(description = description, descriptionError = null) }

    fun onLocationChanged(location: String) =
        _uiState.update { it.copy(location = location) }

    fun onPhotoSelected(uri: Uri) {
        Log.d(TAG, "onPhotoSelected: uri=$uri")
        _uiState.update {
            it.copy(selectedPhotoUri = uri, photoRemoved = false)
        }
        Log.d(TAG, "onPhotoSelected: state.selectedPhotoUri=${_uiState.value.selectedPhotoUri}")
    }

    fun onRemovePhoto() {
        Log.d(TAG, "onRemovePhoto called")
        _uiState.update {
            it.copy(selectedPhotoUri = null, currentPhotoUri = null, photoRemoved = true)
        }
    }

    fun onSave(onSuccess: () -> Unit) {
        val state = _uiState.value
        val id    = itemId ?: run {
            Log.e(TAG, "onSave: ABORT — itemId is null")
            return
        }

        Log.d(TAG, "=== onSave START ===")
        Log.d(TAG, "id               = $id")
        Log.d(TAG, "title            = ${state.title}")
        Log.d(TAG, "selectedPhotoUri = ${state.selectedPhotoUri}")
        Log.d(TAG, "currentPhotoUri  = ${state.currentPhotoUri}")
        Log.d(TAG, "photoRemoved     = ${state.photoRemoved}")

        var hasError = false
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            hasError = true
        }
        if (state.description.isBlank()) {
            _uiState.update { it.copy(descriptionError = "Description is required") }
            hasError = true
        }
        if (hasError) {
            Log.e(TAG, "onSave: ABORT — validation failed")
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val finalPhotoPath: String?
                val photoChanged: Boolean

                when {
                    state.selectedPhotoUri != null -> {
                        Log.d(TAG, "Case A: new photo — calling savePhoto")
                        val saved = photoManager.savePhoto(state.selectedPhotoUri)
                        Log.d(TAG, "savePhoto returned: $saved")

                        if (saved == null) {
                            Log.e(TAG, "ABORT: savePhoto returned null")
                            _uiState.update {
                                it.copy(isSaving = false,
                                    error = "Could not read the selected photo. Please try picking it again.")
                            }
                            return@launch
                        }

                        photoManager.deletePhoto(state.currentPhotoUri)
                        Log.d(TAG, "Old photo deleted")

                        finalPhotoPath = saved
                        photoChanged   = true
                    }

                    state.photoRemoved -> {
                        Log.d(TAG, "Case B: photo removed")
                        photoManager.deletePhoto(state.currentPhotoUri)
                        finalPhotoPath = null
                        photoChanged   = true
                    }

                    else -> {
                        Log.d(TAG, "Case C: photo untouched")
                        finalPhotoPath = state.currentPhotoUri
                        photoChanged   = false
                    }
                }

                Log.d(TAG, "finalPhotoPath = $finalPhotoPath")
                Log.d(TAG, "photoChanged   = $photoChanged")
                Log.d(TAG, "Calling repository.updateItemDetails...")

                val result = repository.updateItemDetails(
                    id           = id,
                    title        = state.title.trim(),
                    description  = state.description.trim(),
                    location     = state.location.trim().ifBlank { null },
                    photoUri     = finalPhotoPath,
                    photoChanged = photoChanged
                )

                Log.d(TAG, "updateItemDetails result isSuccess=${result.isSuccess}")
                if (!result.isSuccess) {
                    Log.e(TAG, "updateItemDetails FAILED: ${result.exceptionOrNull()?.message}")
                }

                if (result.isSuccess) {
                    Log.d(TAG, "=== onSave SUCCESS ===")
                    _uiState.update { EditItemUiState() }
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(isSaving = false,
                            error = result.exceptionOrNull()?.message ?: "Failed to save changes")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "EXCEPTION: ${e::class.simpleName}: ${e.message}")
                e.printStackTrace()
                _uiState.update {
                    it.copy(isSaving = false,
                        error = "Failed to save changes: ${e.message}")
                }
            }
        }
    }
}