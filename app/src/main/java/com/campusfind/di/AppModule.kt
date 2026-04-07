package com.campusfind.di

import android.content.Context
import android.content.SharedPreferences
import com.campusfind.data.local.photo.PhotoManager
import com.campusfind.data.local.preferences.SessionManager
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AppModule.kt — Phase 2
 *
 * Change from Phase 1:
 * - provideSessionManager now injects FirebaseAuth
 *   so SessionManager can use Firebase UID as the source of truth
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("campusfind_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideSessionManager(
        prefs: SharedPreferences,
        firebaseAuth: FirebaseAuth          // ← NEW: Firebase Auth injected
    ): SessionManager {
        return SessionManager(prefs, firebaseAuth)
    }

    @Provides
    @Singleton
    fun providePhotoManager(@ApplicationContext context: Context): PhotoManager {
        return PhotoManager(context)
    }
}