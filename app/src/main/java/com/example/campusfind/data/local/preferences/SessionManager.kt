package com.campusfind.data.local.preferences

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FILE: app/src/main/java/com/campusfind/data/local/preferences/SessionManager.kt
 *
 * CRITICAL: The @Inject constructor annotation tells Hilt it can construct this class
 * automatically by providing SharedPreferences (which comes from AppModule).
 *
 * Without @Inject constructor, Hilt cannot inject SessionManager into ViewModels or UseCases.
 */
@Singleton
class SessionManager @Inject constructor(
    private val prefs: SharedPreferences
) {

    companion object {
        private const val KEY_USER_ID   = "current_user_id"
        private const val KEY_USER_NAME = "current_user_name"
    }

    val currentUserId: String?
        get() = prefs.getString(KEY_USER_ID, null)

    val currentUserName: String?
        get() = prefs.getString(KEY_USER_NAME, null)

    val isLoggedIn: Boolean
        get() = currentUserId != null

    fun saveSession(userId: String, userName: String) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, userName)
            .apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}