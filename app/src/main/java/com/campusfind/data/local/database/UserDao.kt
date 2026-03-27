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
}