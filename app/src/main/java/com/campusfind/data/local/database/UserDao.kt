package com.campusfind.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    // Flow observer — emits immediately on every Room write to this user row.
    // This is what makes profile edits show instantly (offline-first).
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE sync_status = 'PENDING_SYNC'")
    suspend fun getPendingSyncUsers(): List<UserEntity>

    @Query("UPDATE users SET messenger_handle = :messengerHandle WHERE id = :userId")
    suspend fun updateMessengerHandle(userId: String, messengerHandle: String)

    @Query("""
        UPDATE users
        SET full_name = :fullName,
            email = :email,
            sync_status = 'PENDING_SYNC'
        WHERE id = :userId
    """)
    suspend fun updateProfile(userId: String, fullName: String, email: String)

    @Query("""
        UPDATE users
        SET profile_photo_uri = :photoUri,
            sync_status = 'PENDING_SYNC'
        WHERE id = :userId
    """)
    suspend fun updateProfilePhoto(userId: String, photoUri: String)

    @Query("UPDATE users SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)

    @Query("""
        INSERT OR IGNORE INTO users
        (id, full_name, email, password_hash, messenger_handle, created_at, sync_status, profile_photo_uri)
        VALUES (:id, :fullName, :email, :passwordHash, :messengerHandle, :createdAt, 'SYNCED', :profilePhotoUri)
    """)
    suspend fun insertFromRemoteIfAbsent(
        id: String,
        fullName: String,
        email: String,
        passwordHash: String,
        messengerHandle: String?,
        createdAt: Long,
        profilePhotoUri: String? = null
    )

    // Update non-sensitive fields from remote.
    // profile_photo_uri is only updated when remote has a real value.
    // A null from remote never overwrites a locally saved photo path.
    @Query("""
        UPDATE users
        SET full_name = :fullName,
            messenger_handle = :messengerHandle,
            sync_status = 'SYNCED'
        WHERE id = :id AND sync_status != 'PENDING_SYNC'
    """)
    suspend fun updateNonSensitiveFromRemote(
        id: String,
        fullName: String,
        messengerHandle: String?
    )

    // Only called when remote has an actual photo URL (not null)
    @Query("UPDATE users SET profile_photo_uri = :photoUri WHERE id = :id")
    suspend fun updateRemotePhotoUri(id: String, photoUri: String)
}