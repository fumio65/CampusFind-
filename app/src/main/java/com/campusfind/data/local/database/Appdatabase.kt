package com.campusfind.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * FILE: app/src/main/java/com/campusfind/data/local/database/AppDatabase.kt
 *
 * Room database for CampusFind+ — UPDATED to version 2.
 *
 * Version 2 changes (schema migration):
 * - Added `location` column (TEXT, nullable)
 * - Added `photo_uri` column (TEXT, nullable)
 *
 * Migration strategy:
 * - During development: fallbackToDestructiveMigration() drops and recreates
 * - For production: would use addMigration(MIGRATION_1_2)
 *
 * See: DEC-003 (offline-first / SSOT), DEC-006 (Room), DEC-022 (Hilt),
 *      TASK-100a (DatabaseModule), TASK-104, TASK-113 (enhanced)
 */
@Database(
    entities = [
        UserEntity::class,
        LostItemEntity::class
    ],
    version = 2,                    // Bumped from 1 to 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun lostItemDao(): LostItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Get the singleton database instance.
         * Prefer Hilt injection in production code — this is mainly for tests.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campusfind_db"
                )
                    .fallbackToDestructiveMigration()  // Dev only — drops DB on schema change
                    // .addMigration(MIGRATION_1_2)    // Production approach (commented for now)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Clear the singleton instance — used in tests to get a fresh DB
         */
        fun clearInstance() {
            INSTANCE = null
        }

        /**
         * Migration from version 1 to version 2.
         * Adds location and photo_uri columns.
         *
         * Commented out because we're using fallbackToDestructiveMigration() for development.
         * Uncomment this and remove fallbackToDestructiveMigration() for production.
         */
        /*
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE lost_items ADD COLUMN location TEXT")
                database.execSQL("ALTER TABLE lost_items ADD COLUMN photo_uri TEXT")
            }
        }
        */
    }
}