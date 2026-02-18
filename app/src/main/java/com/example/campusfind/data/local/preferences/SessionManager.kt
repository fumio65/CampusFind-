package com.campusfind.data.local.preferences

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FILE: app/src/main/java/com/campusfind/data/local/preferences/SessionManager.kt
 *
 * Manages the current logged-in user session using SharedPreferences.
 *
 * Why this exists:
 * - Session data (current_user_id, current_user_name) is read in multiple places:
 *     NavGraph (auth guard), HomeViewModel, DetailViewModel, AddItemViewModel, etc.
 * - Without SessionManager, every class would call SharedPreferences directly — violates SRP
 * - SessionManager is the single place that knows the key names and how to read/write them
 *
 * Why SharedPreferences (not Room):
 * - Session is a singleton scalar value, not a list or table
 * - Needs to be readable synchronously on the main thread (NavGraph startup decision)
 * - Does not need to be observed over time via Flow
 * - Survives app restart (login persists across device reboot)
 *
 * Why currentUserId is nullable:
 * - null = no user logged in → NavGraph redirects to LoginScreen
 * - non-null = user logged in → NavGraph shows HomeScreen
 *
 * Why provided by AppModule:
 * - SharedPreferences requires a Context to construct — cannot be built by Hilt automatically
 * - AppModule.provideSharedPreferences() creates it with the correct key ("campusfind_prefs")
 * - SessionManager receives it via @Inject constructor — DI in action
 *
 * See: DEC-016 (local auth), DEC-017 (session storage), DEC-022 (Hilt DI),
 *      TASK-100b (AppModule), TASK-105
 */
@Singleton
class SessionManager @Inject constructor(
    private val prefs: SharedPreferences
) {

    // ── Constants ────────────────────────────────────────────────────────────

    companion object {
        private const val KEY_USER_ID   = "current_user_id"
        private const val KEY_USER_NAME = "current_user_name"
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Current logged-in user ID (UUID String).
     * Returns null if no user is logged in.
     *
     * Used by:
     * - NavGraph startDestination logic (if null → LoginScreen, else → HomeScreen)
     * - AddItemViewModel (sets reportedBy)
     * - DetailViewModel (checks ownership: item.reportedBy == currentUserId)
     * - UpdateItemStatusUseCase, DeleteItemUseCase (ownership enforcement)
     */
    val currentUserId: String?
        get() = prefs.getString(KEY_USER_ID, null)

    /**
     * Current logged-in user display name.
     * Returns null if no user is logged in.
     *
     * Used by:
     * - UserProfileScreen (shows "Logged in as [name]")
     * - TopAppBar in future screens (optional greeting)
     */
    val currentUserName: String?
        get() = prefs.getString(KEY_USER_NAME, null)

    /**
     * Check if a user is currently logged in.
     *
     * Used by:
     * - NavGraph auth guard (determines startDestination)
     * - Future middleware / interceptors
     */
    val isLoggedIn: Boolean
        get() = currentUserId != null

    /**
     * Save a user session after successful login or registration.
     *
     * Called by:
     * - LoginViewModel after LoginUseCase returns success
     * - RegisterViewModel after RegisterUseCase returns success
     *
     * @param userId   The user's UUID from UserEntity.id
     * @param userName The user's full name from UserEntity.fullName
     */
    fun saveSession(userId: String, userName: String) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, userName)
            .apply()   // async write — main thread safe
    }

    /**
     * Clear the session — log the user out.
     *
     * Called by:
     * - SettingsViewModel when user taps "Logout" button (TASK-109)
     *
     * After calling this, NavGraph will redirect to LoginScreen on next recomposition
     * or app restart because currentUserId returns null.
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}