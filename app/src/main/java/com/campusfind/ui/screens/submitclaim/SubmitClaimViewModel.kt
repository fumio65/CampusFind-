package com.campusfind.ui.screens.submitclaim

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.repository.ClaimRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubmitClaimViewModel @Inject constructor(
    private val claimRepository: ClaimRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubmitClaimUiState())
    val uiState: StateFlow<SubmitClaimUiState> = _uiState.asStateFlow()

    companion object {
        const val MAX_PHOTOS = 3
        const val MIN_LOCATION_LENGTH = 10
        const val MAX_LOCATION_LENGTH = 500
    }

    fun initialize(itemId: String, itemTitle: String) {
        _uiState.update {
            it.copy(
                itemId = itemId,
                itemTitle = itemTitle
            )
        }
    }

    fun onLocationChanged(location: String) {
        if (location.length <= MAX_LOCATION_LENGTH) {
            _uiState.update { it.copy(location = location) }
        }
    }

    fun onPhotoSelected(uri: Uri) {
        val currentPhotos = _uiState.value.photoUris
        if (currentPhotos.size < MAX_PHOTOS) {
            _uiState.update {
                it.copy(photoUris = currentPhotos + uri)
            }
        }
    }

    fun onRemovePhoto(uri: Uri) {
        _uiState.update {
            it.copy(photoUris = it.photoUris.filter { photoUri -> photoUri != uri })
        }
    }

    fun submitClaim(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        // Validation
        if (currentState.location.isBlank()) {
            _uiState.update { it.copy(error = "Please describe where you found the item") }
            return
        }

        if (currentState.location.length < MIN_LOCATION_LENGTH) {
            _uiState.update { it.copy(error = "Please provide more details (at least 10 characters)") }
            return
        }

        // PHOTO IS REQUIRED
        if (currentState.photoUris.isEmpty()) {
            _uiState.update { it.copy(error = "Photo proof is required. Please add at least one photo.") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            try {
                val userId = sessionManager.currentUserId
                if (userId == null) {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = "You must be logged in to submit a claim"
                        )
                    }
                    return@launch
                }

                // Submit claim with all required parameters
                val result = claimRepository.submitClaim(
                    itemId = currentState.itemId,
                    finderId = userId,
                    message = currentState.location.trim(),
                    photoUrl = currentState.photoUris.firstOrNull()?.toString()
                )

                if (result.isSuccess) {
                    _uiState.update { it.copy(isSubmitting = false) }
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = result.exceptionOrNull()?.message ?: "Failed to submit claim"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = "Failed to submit claim: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}