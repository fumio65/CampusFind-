package com.campusfind.di

import com.campusfind.data.repository.FirebaseAuthRepositoryImpl
import com.campusfind.data.repository.FirestoreClaimRepositoryImpl
import com.campusfind.data.repository.FirestoreLostItemRepositoryImpl
import com.campusfind.data.repository.FirestoreTipRepositoryImpl
import com.campusfind.domain.repository.ClaimRepository
import com.campusfind.domain.repository.LostItemRepository
import com.campusfind.domain.repository.TipRepository
import com.campusfind.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * RepositoryModule.kt — Phase 2 (All Firestore)
 *
 * All repositories now use Firestore sync implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindUserRepository(
        impl: FirebaseAuthRepositoryImpl
    ): UserRepository

    @Binds @Singleton
    abstract fun bindLostItemRepository(
        impl: FirestoreLostItemRepositoryImpl
    ): LostItemRepository

    @Binds @Singleton
    abstract fun bindClaimRepository(
        impl: FirestoreClaimRepositoryImpl
    ): ClaimRepository

    @Binds @Singleton
    abstract fun bindTipRepository(
        impl: FirestoreTipRepositoryImpl
    ): TipRepository
}