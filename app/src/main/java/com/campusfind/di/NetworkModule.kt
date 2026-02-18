package com.campusfind.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * FILE: app/src/main/java/com/campusfind/di/NetworkModule.kt
 *
 * Phase 2 stub — do NOT add Firebase dependencies yet.
 *
 * When Phase 2 starts (TASK-201), add:
 *
 *   @Provides
 *   @Singleton
 *   fun provideFirestore(): FirebaseFirestore {
 *       return FirebaseFirestore.getInstance()
 *   }
 *
 * See: DEC-007 (Firebase), DEC-022 (Hilt), TASK-100d, TASK-201
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // Phase 2 — add FirebaseFirestore provider here (TASK-201)
}