package com.campusfind.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * FirebaseModule.kt
 *
 * Provides Firebase services as singletons via Hilt.
 *
 * Services provided:
 * - FirebaseAuth     — user authentication (replaces SHA-256 local auth)
 * - FirebaseFirestore — cloud database (sync target for Room)
 * - FirebaseStorage  — cloud photo storage (replaces local file paths)
 *
 * Why singleton?
 * - Firebase SDK manages its own connection pooling internally
 * - One instance per app lifetime is the recommended pattern
 * - Matches @Singleton scope of all other data-layer dependencies
 *
 * Offline persistence:
 * - Firestore has built-in offline cache — we enable it explicitly
 * - This means Firestore queries work even without internet
 * - Room remains the primary SSOT; Firestore cache is a bonus
 *
 * See: DEC-007 (Firebase decision), ARCHITECTURE.md (DIP — NetworkModule)
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance().also { db ->
            // Enable offline persistence so Firestore works without internet
            // Room is still the SSOT — this is an extra resilience layer
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            db.firestoreSettings = settings
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorageManager(
        storage: FirebaseStorage,
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
    ): com.campusfind.data.remote.FirebaseStorageManager {
        return com.campusfind.data.remote.FirebaseStorageManager(storage, context)
    }
}