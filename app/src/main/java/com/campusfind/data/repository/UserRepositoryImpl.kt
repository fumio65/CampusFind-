package com.campusfind.data.repository

import com.campusfind.data.local.database.UserDao
import com.campusfind.data.local.database.UserEntity
import com.campusfind.data.sync.SyncManager
import com.campusfind.domain.model.SyncStatus
import com.campusfind.domain.model.User
import com.campusfind.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val syncManager: SyncManager
) : UserRepository {

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        messengerHandle: String?
    ): Result<User> {
        return try {
            val existing = userDao.getUserByEmail(email)
            if (existing != null) return Result.failure(Exception("Email already registered"))

            val userEntity = UserEntity(
                id              = UUID.randomUUID().toString(),
                fullName        = fullName,
                email           = email,
                passwordHash    = hashPassword(password),
                messengerHandle = messengerHandle,
                createdAt       = System.currentTimeMillis(),
                syncStatus      = SyncStatus.PENDING_SYNC.name
            )
            userDao.insertUser(userEntity)
            syncManager.triggerNow()
            Result.success(userEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val userEntity = userDao.getUserByEmail(email)
                ?: return Result.failure(Exception("No account found with this email"))
            if (userEntity.passwordHash != hashPassword(password))
                return Result.failure(Exception("Incorrect password"))
            Result.success(userEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(id: String): User? =
        userDao.getUserById(id)?.toDomain()

    override fun observeUser(id: String): Flow<User?> =
        userDao.observeUserById(id).map { it?.toDomain() }

    override suspend fun updateMessengerHandle(userId: String, messengerHandle: String): Result<Unit> {
        return try {
            userDao.updateMessengerHandle(userId, messengerHandle)
            userDao.updateSyncStatus(userId, SyncStatus.PENDING_SYNC.name)
            syncManager.triggerNow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Saves name + email locally first (offline-first), then triggers sync to Supabase
    override suspend fun updateProfile(userId: String, fullName: String, email: String): Result<Unit> {
        return try {
            userDao.updateProfile(userId, fullName, email)
            syncManager.triggerNow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Saves photo uri locally first, then triggers sync to Supabase
    override suspend fun updateProfilePhoto(userId: String, photoUri: String): Result<Unit> {
        return try {
            android.util.Log.d("PROFILE_DEBUG", "RepositoryImpl.updateProfilePhoto: userId=$userId path=$photoUri")
            userDao.updateProfilePhoto(userId, photoUri)
            // Verify it was written
            val verify = userDao.getUserById(userId)
            android.util.Log.d("PROFILE_DEBUG", "after DAO write: profile_photo_uri=${verify?.profilePhotoUri}")
            syncManager.triggerNow()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("PROFILE_DEBUG", "RepositoryImpl.updateProfilePhoto FAILED: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun UserEntity.toDomain() = User(
        id              = id,
        fullName        = fullName,
        email           = email,
        messengerHandle = messengerHandle,
        createdAt       = createdAt,
        profilePhotoUri = profilePhotoUri
    )
}