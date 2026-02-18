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
 * Why this exists:
 * - SettingsScreen is not just a static UI — it has one action: logout
 * - Logout needs to call SessionManager.clearSession()
 * - SessionManager is provided by Hilt — ViewModel receives it via @Inject
 *
 * Why logout is synchronous (not suspend):
 * - SessionManager.clearSession() calls SharedPreferences.edit().clear().apply()
 * - apply() is async internally — main thread safe
 * - No need for viewModelScope.launch
 *
 * Why no UI state:
 * - Settings has no loading states or error states
 * - It's just static preferences + one logout action
 * - If we add more features later (theme toggle, notification settings), we'd add state
 *
 * See: DEC-001 (MVVM), DEC-017 (session storage), DEC-022 (Hilt DI),
 *      TASK-105 (SessionManager), TASK-109, demo steps 6 & 11
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    /**
     * Log the user out.
     *
     * Clears the session from SharedPreferences.
     * After this, SessionManager.isLoggedIn returns false,
     * and the NavGraph auth guard will redirect to LoginScreen
     * on next recomposition or app restart.
     *
     * Called by: SettingsScreen logout button onClick
     */
    fun logout() {
        sessionManager.clearSession()
    }
}