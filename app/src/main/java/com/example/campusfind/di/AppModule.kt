package com.campusfind.di

import android.content.Context
import android.content.SharedPreferences
import com.campusfind.data.local.preferences.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * FILE: app/src/main/java/com/campusfind/di/AppModule.kt
 *
 * Provides app-wide utilities — SharedPreferences and SessionManager.
 * Uses @Provides because both require manual construction.
 *
 * SharedPreferences is provided here so SessionManager receives it
 * via constructor injection rather than creating it itself (DIP).
 *
 * See: DEC-017 (session storage), DEC-022 (Hilt), TASK-100b
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences("campusfind_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideSessionManager(
        prefs: SharedPreferences
    ): SessionManager {
        return SessionManager(prefs)
    }
}