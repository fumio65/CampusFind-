package com.campusfind.ui.screens.register

/**
 * RegisterUiState - UI state for registration screen
 *
 * UPDATED: messengerHandle is now required (but still nullable in state for validation)
 * - Validation happens in RegisterViewModel.onRegisterClicked()
 * - Error message shown if user tries to register without messenger username
 *
 * @param fullName User's full name
 * @param email University email
 * @param password Password (8+ characters)
 * @param confirmPassword Must match password
 * @param messengerHandle Messenger username (REQUIRED for registration)
 * @param isSubmitting True while registration API call is in progress
 * @param error Error message to display (null if no error)
 */
data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val messengerHandle: String? = null,  // Nullable for validation, but REQUIRED before submit
    val isSubmitting: Boolean = false,
    val error: String? = null
)