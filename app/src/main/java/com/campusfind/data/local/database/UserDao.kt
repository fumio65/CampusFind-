package com.campusfind.data.local.database

import androidx.room.*

/**
 * UserDao - COMPLETE WITH ALL QUERIES
 *
 * Room DAO for users table operations
 */
@Dao
interface UserDao {

    // ══════════════════════════════════════
    // INSERT METHODS
    // ══════════════════════════════════════

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    // ══════════════════════════════════════
    // QUERY METHODS
    // ══════════════════════════════════════

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    // ══════════════════════════════════════
    // UPDATE METHODS
    // ══════════════════════════════════════

    /**
     * Update user's Messenger handle (for UserProfileScreen Add/Edit)
     */
    @Query("""
        UPDATE users
        SET messenger_handle = :messengerHandle
        WHERE id = :userId
    """)
    suspend fun updateMessengerHandle(userId: String, messengerHandle: String)

    @Query("SELECT * FROM users WHERE sync_status = 'PENDING_SYNC'")
    suspend fun getPendingSyncUsers(): List<UserEntity>

    @Query("UPDATE users SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)

    // Upsert a remote user without overwriting an existing local password_hash
    @Query("""
        INSERT OR REPLACE INTO users (id, full_name, email, password_hash, messenger_handle, created_at, sync_status)
        VALUES (:id, :fullName, :email,
                COALESCE((SELECT password_hash FROM users WHERE id = :id), ''),
                :messengerHandle, :createdAt, 'SYNCED')
    """)
    suspend fun upsertFromRemote(
        id: String,
        fullName: String,
        email: String,
        messengerHandle: String?,
        createdAt: Long
    )
}