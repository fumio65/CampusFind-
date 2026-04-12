package com.campusfind.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        LostItemEntity::class,
        TipEntity::class,
        ClaimEntity::class,
        ClaimReplyEntity::class
    ],
    version = 4,          // bumped from 3 → 4 for profile_photo_uri column
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun lostItemDao(): LostItemDao
    abstract fun tipDao(): TipDao
    abstract fun claimDao(): ClaimDao
    abstract fun claimReplyDao(): ClaimReplyDao

    companion object {

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE lost_items ADD COLUMN sync_status TEXT NOT NULL DEFAULT 'PENDING_SYNC'")
                db.execSQL("ALTER TABLE users ADD COLUMN sync_status TEXT NOT NULL DEFAULT 'PENDING_SYNC'")
                db.execSQL("ALTER TABLE claims ADD COLUMN sync_status TEXT NOT NULL DEFAULT 'PENDING_SYNC'")
                db.execSQL("ALTER TABLE claim_replies ADD COLUMN sync_status TEXT NOT NULL DEFAULT 'PENDING_SYNC'")
                db.execSQL("ALTER TABLE tips ADD COLUMN sync_status TEXT NOT NULL DEFAULT 'PENDING_SYNC'")
            }
        }

        // Adds profile_photo_uri to users table
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN profile_photo_uri TEXT")
            }
        }
    }
}