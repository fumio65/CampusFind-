package com.campusfind.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/register/RegisterViewModel.kt
 *
 * ViewModel for the registration screen.
 *
 * UPDATED: Messenger username is now REQUIRED for claim communication
 * - Validates that messengerHandle is not blank
 * - Auto-adds @ prefix if user doesn't include it
 * - Blocks registration if messenger username is empty
 *
 * Why @HiltViewModel:
 * - Tells Hilt to manage this ViewModel's lifecycle and inject dependencies
 * - RegisterScreen calls hiltViewModel() to get this ViewModel automatically
 * - No manual ViewModel factory needed
 *
 * Why @Inject constructor with RegisterUseCase and SessionManager:
 * - RegisterUseCase handles the business logic (validation + repository call)
 * - SessionManager saves the session after successful registration
 * - Both are injected by Hilt — ViewModel never constructs them directly (DI + DIP)
 *
 * Why StateFlow instead of LiveData:
 * - StateFlow is Compose-native — works seamlessly with collectAsState()
 * - Type-safe, null-safe, Flow-based — integrates with coroutines
 * - LiveData is legacy Android — StateFlow is the modern replacement
 *
 * Why private _uiState + public uiState pattern:
 * - _uiState is mutable (MutableStateFlow) — only the ViewModel can update it
 * - uiState is read-only (StateFlow) — the UI can only observe, never modify
 * - Enforces unidirectional data flow: UI events → ViewModel → state update → UI recompose
 *
 * See: DEC-001 (MVVM), DEC-022 (Hilt DI), TASK-106 (RegisterUseCase),
 *      TASK-107 (RegisterScreen)
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // ── TEXT FIELD UPDATES ───────────────────────────────────────────────────

    fun onFullNameChanged(value: String) {
        _uiState.update { it.copy(fullName = value, error = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPassword = value, error = null) }
    }

    fun onMessengerHandleChanged(value: String) {
        // Auto-add @ prefix if user doesn't include it
        val formattedValue = if (value.isNotBlank() && !value.startsWith("@")) {
            "@$value"
        } else {
            value
        }
        _uiState.update { it.copy(messengerHandle = formattedValue, error = null) }
    }

    // ── REGISTRATION ─────────────────────────────────────────────────────────

    /**
     * Handle the register button click.
     *
     * UPDATED: Messenger username is now REQUIRED
     * - Validates that messengerHandle is not blank
     * - Shows error: "Messenger username is required for coordinating item returns"
     *
     * Validation + repository call delegated to RegisterUseCase.
     *
     * On success:
     * - Save session (userId, userName, userEmail) via SessionManager
     * - Call onSuccess() callback to navigate to HomeScreen
     *
     * On failure:
     * - Set error message in UI state
     * - User sees the error: "Passwords do not match", "Email already exists", etc.
     *
     * @param onSuccess Called when registration succeeds — navigate to HomeScreen
     */
    fun onRegisterClicked(onSuccess: () -> Unit) {
        // Prevent double-submission while in progress
        if (_uiState.value.isSubmitting) return

        val currentState = _uiState.value

        // ✅ VALIDATION: Messenger username is REQUIRED
        if (currentState.messengerHandle.isNullOrBlank()) {
            _uiState.update {
                it.copy(error = "Messenger username is required for coordinating item returns")
            }
            return
        }

        // ✅ VALIDATION: Messenger username must be at least 3 characters (including @)
        if (currentState.messengerHandle.length < 3) {
            _uiState.update {
                it.copy(error = "Messenger username must be at least 2 characters")
            }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            // Remove @ prefix before saving (stored as "username" not "@username")
            val cleanMessengerHandle = currentState.messengerHandle
                ?.removePrefix("@")
                ?.trim()

            val result = registerUseCase(
                fullName        = currentState.fullName,
                email           = currentState.email,
                password        = currentState.password,
                confirmPassword = currentState.confirmPassword,
                messengerHandle = cleanMessengerHandle  // ✅ Now guaranteed to be non-null
            )

            result.fold(
                onSuccess = { user ->
                    // Save session (now includes email)
                    sessionManager.saveSession(
                        userId = user.id,
                        userName = user.fullName,
                        userEmail = user.email
                    )
                    _uiState.update { it.copy(isSubmitting = false) }
                    // Navigate to Home (callback clears back stack)
                    onSuccess()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = exception.message ?: "Registration failed"
                        )
                    }
                }
            )
        }
    }
}