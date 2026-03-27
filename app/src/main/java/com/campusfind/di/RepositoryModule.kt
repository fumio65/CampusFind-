package com.campusfind.di

import com.campusfind.data.repository.ClaimRepositoryImpl
import com.campusfind.data.repository.LostItemRepositoryImpl
import com.campusfind.data.repository.TipRepositoryImpl
import com.campusfind.data.repository.UserRepositoryImpl
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
 * FILE: app/src/main/java/com/campusfind/di/RepositoryModule.kt
 *
 * Hilt module for repository bindings.
 *
 * UPDATED: Added TipRepository binding for Phase 7
 *
 * Why @Binds?
 * - More efficient than @Provides for simple interface → impl mapping
 * - Enforces DIP: ViewModels depend on interfaces only
 *
 * See: DEC-022 (Hilt DI, DIP), Phase 6 Claims Feature, Phase 7 Tips Feature
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLostItemRepository(
        impl: LostItemRepositoryImpl
    ): LostItemRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindClaimRepository(
        impl: ClaimRepositoryImpl
    ): ClaimRepository

    @Binds
    @Singleton
    abstract fun bindTipRepository(
        impl: TipRepositoryImpl
    ): TipRepository
}