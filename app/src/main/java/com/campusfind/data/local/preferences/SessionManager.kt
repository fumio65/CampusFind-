package com.campusfind.data.local.preferences

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val prefs: SharedPreferences
) {
    companion object {
        private const val PREF_CURRENT_USER_ID      = "current_user_id"
        private const val PREF_CURRENT_USER_NAME    = "current_user_name"
        private const val PREF_CURRENT_USER_EMAIL   = "current_user_email"
        private const val PREF_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val PREF_DARK_MODE            = "dark_mode"
    }

    val currentUserId: String?
        get() = prefs.getString(PREF_CURRENT_USER_ID, null)

    val currentUserName: String?
        get() = prefs.getString(PREF_CURRENT_USER_NAME, null)

    val currentUserEmail: String?
        get() = prefs.getString(PREF_CURRENT_USER_EMAIL, null)

    val isLoggedIn: Boolean
        get() = currentUserId != null

    val hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(PREF_ONBOARDING_COMPLETED, false)

    // ── Dark mode — reactive StateFlow so MainActivity recomposes instantly ──
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean(PREF_DARK_MODE, false))
    val isDarkModeFlow: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Synchronous read — used during init before flow is collected
    val isDarkMode: Boolean
        get() = _isDarkMode.value

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_DARK_MODE, enabled).apply()
        _isDarkMode.value = enabled   // triggers MainActivity recomposition instantly
    }

    fun saveSession(userId: String, userName: String, userEmail: String) {
        prefs.edit()
            .putString(PREF_CURRENT_USER_ID, userId)
            .putString(PREF_CURRENT_USER_NAME, userName)
            .putString(PREF_CURRENT_USER_EMAIL, userEmail)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove(PREF_CURRENT_USER_ID)
            .remove(PREF_CURRENT_USER_NAME)
            .remove(PREF_CURRENT_USER_EMAIL)
            .apply()
    }

    fun markOnboardingCompleted() {
        prefs.edit().putBoolean(PREF_ONBOARDING_COMPLETED, true).apply()
    }
}