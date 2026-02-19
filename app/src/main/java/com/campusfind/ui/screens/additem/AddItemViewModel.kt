package com.campusfind.ui.screens.additem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.domain.usecase.AddItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/additem/AddItemViewModel.kt
 *
 * ViewModel for the add item screen.
 *
 * UPDATED: Now handles location and photoUri state.
 *
 * See: DEC-001 (MVVM), DEC-022 (Hilt DI), TASK-113
 */
@HiltViewModel
class AddItemViewModel @Inject constructor(
    private val addItemUseCase: AddItemUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddItemUiState())
    val uiState: StateFlow<AddItemUiState> = _uiState.asStateFlow()

    // ── TEXT FIELD UPDATES ───────────────────────────────────────────────────

    fun onTitleChanged(value: String) {
        _uiState.update { it.copy(title = value, error = null) }
    }

    fun onDescriptionChanged(value: String) {
        _uiState.update { it.copy(description = value, error = null) }
    }

    fun onLocationChanged(value: String) {
        _uiState.update { it.copy(location = value, error = null) }
    }

    // ── PHOTO PICKER ─────────────────────────────────────────────────────────

    /**
     * Called when user picks a photo.
     * @param uri Android content URI (e.g., content://media/external/images/media/1234)
     */
    fun onPhotoSelected(uri: String?) {
        _uiState.update { it.copy(photoUri = uri) }
    }

    // ── SUBMIT ───────────────────────────────────────────────────────────────

    fun onSubmit(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        // Prevent double-submission
        if (currentState.isSubmitting) return

        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            val result = addItemUseCase(
                title = currentState.title,
                description = currentState.description,
                location = currentState.location.ifBlank { null },
                photoUri = currentState.photoUri
            )

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSubmitting = false) }
                    onSuccess()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = exception.message ?: "Failed to create report"
                        )
                    }
                }
            )
        }
    }
}