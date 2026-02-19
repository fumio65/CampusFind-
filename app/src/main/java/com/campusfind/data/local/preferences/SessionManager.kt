package com.campusfind.data.local.preferences

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FILE: app/src/main/java/com/campusfind/data/local/preferences/SessionManager.kt
 *
 * Manages user session and app state in SharedPreferences.
 *
 * UPDATED: Added onboarding completion tracking.
 *
 * Why SharedPreferences:
 * - Persists across app restarts
 * - Synchronous reads on main thread (safe for navigation decisions)
 * - Survives process death
 *
 * Session data:
 * - current_user_id: UUID of logged-in user (null if logged out)
 * - current_user_name: Display name (for UI convenience)
 * - onboarding_completed: Boolean flag (first install check)
 *
 * See: DEC-017 (session storage), TASK-105, TASK-118
 */
@Singleton
class SessionManager @Inject constructor(
    private val prefs: SharedPreferences
) {

    companion object {
        private const val PREF_CURRENT_USER_ID = "current_user_id"
        private const val PREF_CURRENT_USER_NAME = "current_user_name"
        private const val PREF_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    // ── SESSION ──────────────────────────────────────────────────────────────

    val currentUserId: String?
        get() = prefs.getString(PREF_CURRENT_USER_ID, null)

    val currentUserName: String?
        get() = prefs.getString(PREF_CURRENT_USER_NAME, null)

    val isLoggedIn: Boolean
        get() = currentUserId != null

    fun saveSession(userId: String, userName: String) {
        prefs.edit()
            .putString(PREF_CURRENT_USER_ID, userId)
            .putString(PREF_CURRENT_USER_NAME, userName)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove(PREF_CURRENT_USER_ID)
            .remove(PREF_CURRENT_USER_NAME)
            .apply()
    }

    // ── ONBOARDING ───────────────────────────────────────────────────────────

    val hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(PREF_ONBOARDING_COMPLETED, false)

    fun markOnboardingCompleted() {
        prefs.edit()
            .putBoolean(PREF_ONBOARDING_COMPLETED, true)
            .apply()
    }
}