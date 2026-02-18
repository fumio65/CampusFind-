package com.campusfind.di

import com.campusfind.data.repository.LostItemRepositoryImpl
import com.campusfind.data.repository.UserRepositoryImpl
import com.campusfind.domain.repository.LostItemRepository
import com.campusfind.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * FILE: app/src/main/java/com/campusfind/di/RepositoryModule.kt
 *
 * This is where the Dependency Inversion Principle (DIP) is enforced.
 *
 * @Binds tells Hilt: "when anyone asks for LostItemRepository (interface),
 * inject LostItemRepositoryImpl (concrete class)."
 *
 * ViewModels and UseCases only ever see the interface — they never import
 * anything from the data/ package directly.
 *
 * @Binds is used instead of @Provides because Hilt can construct the Impl
 * classes automatically via their @Inject constructors — we only need to
 * declare the interface-to-implementation mapping here.
 *
 * Phase 1 → Phase 2 migration: change the @Binds target to
 * CloudSyncLostItemRepositoryImpl — zero changes anywhere else.
 *
 * See: DEC-002 (Repository Pattern), DEC-022 (Hilt/DIP), TASK-100c
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
}