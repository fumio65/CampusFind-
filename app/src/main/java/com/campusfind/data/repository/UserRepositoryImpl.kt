package com.campusfind.data.repository

import com.campusfind.data.local.database.UserDao
import com.campusfind.data.local.database.UserEntity
import com.campusfind.domain.model.User
import com.campusfind.domain.repository.UserRepository
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        messengerHandle: String?
    ): Result<User> {
        return try {
            val existing = userDao.getUserByEmail(email)
            if (existing != null) {
                return Result.failure(Exception("Email already registered"))
            }

            val passwordHash = hashPassword(password)

            val userEntity = UserEntity(
                id = UUID.randomUUID().toString(),
                fullName = fullName,
                email = email,
                passwordHash = passwordHash,
                messengerHandle = messengerHandle,
                createdAt = System.currentTimeMillis()
            )

            userDao.insertUser(userEntity)
            Result.success(userEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<User> {
        return try {
            val userEntity = userDao.getUserByEmail(email)
                ?: return Result.failure(Exception("No account found with this email"))

            val passwordHash = hashPassword(password)
            if (userEntity.passwordHash != passwordHash) {
                return Result.failure(Exception("Incorrect password"))
            }

            Result.success(userEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(id: String): User? {
        return userDao.getUserById(id)?.toDomain()
    }

    override suspend fun updateMessengerHandle(
        userId: String,
        messengerHandle: String
    ): Result<Unit> {
        return try {
            userDao.updateMessengerHandle(userId, messengerHandle)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun UserEntity.toDomain() = User(
        id = id,
        fullName = fullName,
        email = email,
        messengerHandle = messengerHandle,
        createdAt = createdAt
    )
}

