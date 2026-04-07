package com.campusfind.data.local.preferences

import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SessionManager.kt — Phase 2 (Firebase Auth)
 *
 * Changes from Phase 1:
 * - currentUserId now returns Firebase UID (firebaseAuth.currentUser?.uid)
 *   falling back to SharedPreferences for offline resilience
 * - isLoggedIn checks Firebase Auth state first
 * - saveSession still caches name/email locally for offline display
 * - clearSession now also calls firebaseAuth.signOut()
 *
 * Why keep SharedPreferences?
 * - User name and email are displayed in the UI (NavDrawer, ProfileScreen)
 * - We don't want a Firestore call just to show "Hello, Maria"
 * - SharedPreferences gives instant access to display info offline
 *
 * See: DEC-016 (auth migration), DEC-017 (secure storage)
 */
@Singleton
class SessionManager @Inject constructor(
    private val prefs: SharedPreferences,
    private val firebaseAuth: FirebaseAuth      // ← NEW: injected for Phase 2
) {
    companion object {
        private const val PREF_CURRENT_USER_ID      = "current_user_id"
        private const val PREF_CURRENT_USER_NAME    = "current_user_name"
        private const val PREF_CURRENT_USER_EMAIL   = "current_user_email"
        private const val PREF_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val PREF_DARK_MODE            = "dark_mode"
    }

    // ── User ID — Firebase UID is source of truth ──────────────────────────
    // Falls back to SharedPreferences if Firebase Auth is briefly unavailable
    val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid
            ?: prefs.getString(PREF_CURRENT_USER_ID, null)

    val currentUserName: String?
        get() = prefs.getString(PREF_CURRENT_USER_NAME, null)

    val currentUserEmail: String?
        get() = prefs.getString(PREF_CURRENT_USER_EMAIL, null)

    // ── isLoggedIn — Firebase Auth state is source of truth ───────────────
    val isLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null
                || prefs.getString(PREF_CURRENT_USER_ID, null) != null

    val hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(PREF_ONBOARDING_COMPLETED, false)

    // ── Dark mode — reactive StateFlow so MainActivity recomposes instantly ─
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean(PREF_DARK_MODE, false))
    val isDarkModeFlow: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    val isDarkMode: Boolean
        get() = _isDarkMode.value

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_DARK_MODE, enabled).apply()
        _isDarkMode.value = enabled
    }

    // ── Save session — caches display info locally for offline use ─────────
    fun saveSession(userId: String, userName: String, userEmail: String) {
        prefs.edit()
            .putString(PREF_CURRENT_USER_ID,    userId)
            .putString(PREF_CURRENT_USER_NAME,  userName)
            .putString(PREF_CURRENT_USER_EMAIL, userEmail)
            .apply()
    }

    // ── Clear session — signs out of Firebase + clears local cache ─────────
    fun clearSession() {
        firebaseAuth.signOut()              // ← NEW: Firebase sign out
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