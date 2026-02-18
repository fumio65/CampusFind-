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
 * IMPORTANT: This module provides SharedPreferences, and SessionManager
 * constructs itself via @Inject constructor by receiving SharedPreferences.
 *
 * We do NOT need a provideSessionManager() method here because SessionManager
 * has @Inject constructor — Hilt builds it automatically.
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

    // SessionManager is constructed automatically via @Inject constructor
    // No need for provideSessionManager() method
}