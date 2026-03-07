package com.campusfind.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/login/LoginViewModel.kt
 *
 * ViewModel for the login screen.
 *
 * UPDATED: Now saves email to session for Settings/Profile screens.
 *
 * Why @HiltViewModel:
 * - Hilt manages lifecycle and injects LoginUseCase + SessionManager
 * - LoginScreen calls hiltViewModel() to get this instance automatically
 *
 * Why LoginUseCase instead of calling UserRepository directly:
 * - SRP — LoginViewModel only manages UI state
 * - Business logic (validation, password hashing check) lives in LoginUseCase
 * - ViewModel stays thin and testable
 *
 * Flow after successful login:
 * 1. LoginUseCase returns Result.success(User)
 * 2. SessionManager.saveSession(user.id, user.fullName, user.email)  ← UPDATED
 * 3. onSuccess() callback navigates to HomeScreen and clears back stack
 * 4. NavGraph auth guard sees isLoggedIn = true on next app launch
 *
 * See: DEC-001 (MVVM), DEC-022 (Hilt DI), TASK-106 (LoginUseCase),
 *      TASK-108, demo steps 3, 8, 12
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // ── TEXT FIELD UPDATES ───────────────────────────────────────────────────

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    // ── LOGIN ────────────────────────────────────────────────────────────────

    /**
     * Handle the login button click.
     *
     * On success:
     * - Save session (userId, userName, userEmail) via SessionManager  ← UPDATED
     * - Call onSuccess() callback to navigate to HomeScreen
     *
     * On failure:
     * - Set error message in UI state
     * - User sees the error: "No account found" or "Incorrect password"
     *
     * @param onSuccess Called when login succeeds — navigate to HomeScreen
     */
    fun onLoginClicked(onSuccess: () -> Unit) {
        // Prevent double-submission
        if (_uiState.value.isSubmitting) return

        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            val result = loginUseCase(
                email = _uiState.value.email,
                password = _uiState.value.password
            )

            result.fold(
                onSuccess = { user ->
                    // Save session (now includes email)
                    sessionManager.saveSession(
                        userId = user.id,
                        userName = user.fullName,
                        userEmail = user.email  // ← ADDED
                    )
                    _uiState.update { it.copy(isSubmitting = false) }
                    // Navigate to Home (callback clears back stack)
                    onSuccess()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = exception.message ?: "Login failed"
                        )
                    }
                }
            )
        }
    }
}