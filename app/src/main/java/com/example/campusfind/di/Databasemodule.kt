package com.campusfind.di

import android.content.Context
import androidx.room.Room
import com.campusfind.data.local.database.AppDatabase
import com.campusfind.data.local.database.LostItemDao
import com.campusfind.data.local.database.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * FILE: app/src/main/java/com/campusfind/di/DatabaseModule.kt
 *
 * Provides the Room database and both DAOs as app-scoped singletons.
 * Uses @Provides (not @Binds) because Room requires manual construction
 * via the builder pattern — Hilt cannot construct it automatically.
 *
 * See: DEC-006 (Room), DEC-022 (Hilt), TASK-100a
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "campusfind_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideLostItemDao(database: AppDatabase): LostItemDao {
        return database.lostItemDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}