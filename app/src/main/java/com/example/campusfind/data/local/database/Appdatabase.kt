package com.campusfind.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * FILE: app/src/main/java/com/campusfind/data/local/database/AppDatabase.kt
 *
 * Room database for CampusFind+ — Single Source of Truth (SSOT) for all data.
 *
 * Why this design:
 * - Version 1 — matches Phase 1 schema with 2 tables: users + lost_items
 * - exportSchema = false — we are not versioning schema files for this academic project
 * - fallbackToDestructiveMigration() — during development, schema changes drop and recreate
 *   tables instead of requiring manual migrations (acceptable for Phase 1 since data is local)
 * - Singleton pattern — exactly one database instance per app process, shared by all DAOs
 *   (prevents data corruption from multiple DB connections)
 *
 * Phase 2 migration:
 * - Bump version to 2
 * - Remove fallbackToDestructiveMigration()
 * - Add Migration(1, 2) to add sync_status and last_synced_at columns (TASK-206)
 *
 * Why provided by DatabaseModule instead of constructed directly:
 * - Room.databaseBuilder() requires a Context — Hilt provides this via @ApplicationContext
 * - Singleton scope managed by Hilt — one instance for the entire app lifetime (DEC-022)
 * - DAOs extracted from this database are also provided as singletons by DatabaseModule
 *
 * See: DEC-003 (offline-first / SSOT), DEC-006 (Room), DEC-022 (Hilt),
 *      TASK-100a (DatabaseModule), TASK-104
 */
@Database(
    entities = [
        UserEntity::class,
        LostItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // ── DAO Accessors ────────────────────────────────────────────────────────
    // These are called by DatabaseModule to provide DAOs as singletons

    abstract fun userDao(): UserDao

    abstract fun lostItemDao(): LostItemDao

    // ── Singleton Pattern (BACKUP — Hilt is primary provider) ───────────────
    // Hilt provides the database via DatabaseModule.provideDatabase()
    // This companion object is kept as a fallback for non-Hilt contexts (tests)

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
                    .fallbackToDestructiveMigration()
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
    }
}