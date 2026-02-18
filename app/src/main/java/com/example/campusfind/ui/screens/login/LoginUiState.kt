package com.campusfind.ui.screens.login

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/login/LoginUiState.kt
 *
 * UI state for the login screen.
 *
 * Why simpler than RegisterUiState:
 * - Only 2 fields (email + password) vs 6 fields
 * - No confirm password needed
 * - Same pattern: isSubmitting prevents double-tap, error shows at top
 *
 * See: DEC-001 (MVVM), TASK-108
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null
)