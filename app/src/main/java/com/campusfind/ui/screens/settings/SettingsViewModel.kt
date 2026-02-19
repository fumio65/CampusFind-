package com.campusfind.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.campusfind.data.local.preferences.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/settings/SettingsViewModel.kt
 *
 * ViewModel for the settings screen.
 *
 * ENHANCED: Exposes current user name for display.
 *
 * Why @HiltViewModel:
 * - Hilt injects SessionManager automatically
 * - SettingsScreen calls hiltViewModel() to get this instance
 *
 * Why logout() is synchronous:
 * - SessionManager.clearSession() calls SharedPreferences.edit().apply()
 * - apply() is async internally, so the function returns immediately
 * - Navigation happens immediately after (nav to Login)
 *
 * See: DEC-001 (MVVM), DEC-022 (Hilt DI), TASK-109, TASK-119
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    /**
     * Current user's display name from session.
     * Used to show "Logged in as [name]" in the UI.
     */
    val currentUserName: String?
        get() = sessionManager.currentUserName

    /**
     * Clear the user session (logout).
     * Removes current_user_id and current_user_name from SharedPreferences.
     */
    fun logout() {
        sessionManager.clearSession()
    }
}