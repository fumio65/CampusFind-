package com.campusfind.ui.screens.register

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/register/RegisterUiState.kt
 *
 * UI state for the registration screen.
 *
 * Why a separate data class:
 * - All mutable state is centralized in one place
 * - Easy to update via copy() — immutable state updates
 * - Easy to test — just compare two data class instances
 *
 * Why isSubmitting:
 * - Prevents double-submission if user taps the button twice quickly
 * - Shows loading indicator on the submit button while RegisterUseCase is running
 * - Button is disabled when isSubmitting = true
 *
 * Why error is nullable:
 * - null = no error shown
 * - non-null = show error message at the top of the form
 *
 * See: DEC-001 (MVVM), TASK-107
 */
data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val messengerHandle: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null
)