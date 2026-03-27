package com.campusfind.di

import android.content.Context
import androidx.room.Room
import com.campusfind.data.local.database.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "campusfind_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideLostItemDao(database: AppDatabase): LostItemDao {
        return database.lostItemDao()
    }

    @Provides
    @Singleton
    fun provideTipDao(database: AppDatabase): TipDao {
        return database.tipDao()
    }

    @Provides
    @Singleton
    fun provideClaimDao(database: AppDatabase): ClaimDao {
        return database.claimDao()
    }

    @Provides
    @Singleton
    fun provideClaimReplyDao(database: AppDatabase): ClaimReplyDao {
        return database.claimReplyDao()
    }
}