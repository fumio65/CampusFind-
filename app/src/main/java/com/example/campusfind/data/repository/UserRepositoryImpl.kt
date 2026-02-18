package com.campusfind.data.repository

import com.campusfind.data.local.database.UserDao
import com.campusfind.data.local.database.UserEntity
import com.example.campusfind.domain.repository.User
import com.campusfind.domain.repository.UserRepository
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FILE: app/src/main/java/com/campusfind/data/repository/UserRepositoryImpl.kt
 *
 * Implementation of UserRepository using Room for local storage and SHA-256 for password hashing.
 *
 * Why @Inject constructor:
 * - Tells Hilt it can construct this class automatically by providing UserDao
 * - UserDao is provided by DatabaseModule (TASK-100a)
 * - No manual construction needed — Hilt wires everything at compile time
 *
 * Why @Singleton:
 * - Repository should be shared across the entire app — one instance for consistency
 * - Multiple instances could cause race conditions on database writes
 *
 * Why SHA-256 for password hashing:
 * - Acceptable for academic scope (DEC-016)
 * - Better than plain text, worse than bcrypt (bcrypt would require external library)
 * - Phase 2 replaces this with Firebase Auth — hashing moves to server
 *
 * Why Result<User> wrapper:
 * - register() can fail if email already exists (Room throws SQLiteConstraintException)
 * - login() can fail if email not found or password incorrect
 * - Result.success / Result.failure makes error handling explicit in UseCases
 *
 * See: DEC-002 (Repository), DEC-009 (UUID), DEC-016 (local auth),
 *      DEC-022 (Hilt DI), TASK-100c (bound by RepositoryModule), TASK-106
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    // ── REGISTER ─────────────────────────────────────────────────────────────

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        messengerHandle: String?
    ): Result<User> {
        return try {
            val userId = UUID.randomUUID().toString()
            val passwordHash = hashPassword(password)
            val timestamp = System.currentTimeMillis()

            val entity = UserEntity(
                id              = userId,
                fullName        = fullName,
                email           = email,
                passwordHash    = passwordHash,
                messengerHandle = messengerHandle,
                createdAt       = timestamp
            )

            userDao.insertUser(entity)   // throws if email already exists (unique index)

            Result.success(entity.toDomainModel())

        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            // Unique index on email was violated — email already exists
            Result.failure(Exception("Email already in use"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── LOGIN ────────────────────────────────────────────────────────────────

    override suspend fun login(
        email: String,
        password: String
    ): Result<User> {
        return try {
            val entity = userDao.getUserByEmail(email)
                ?: return Result.failure(Exception("No account found with that email"))

            val inputHash = hashPassword(password)
            if (inputHash != entity.passwordHash) {
                return Result.failure(Exception("Incorrect password"))
            }

            Result.success(entity.toDomainModel())

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── GET USER BY ID ───────────────────────────────────────────────────────

    override suspend fun getUserById(id: String): User? {
        return userDao.getUserById(id)?.toDomainModel()
    }

    // ── PASSWORD HASHING ─────────────────────────────────────────────────────

    /**
     * Hash a plain text password using SHA-256.
     *
     * Why SHA-256:
     * - No external dependencies (uses java.security.MessageDigest from Android SDK)
     * - Better than plain text storage
     * - Acceptable for Phase 1 academic scope (DEC-016)
     * - Phase 2 replaces this with Firebase Auth which uses scrypt server-side
     *
     * NOT cryptographically strong by production standards — bcrypt or Argon2 would be better.
     * This is sufficient for MCO 1 where the database is local-only and never leaves the device.
     */
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // ── ENTITY ↔ DOMAIN MAPPING ──────────────────────────────────────────────

    /**
     * Convert UserEntity (data layer) to User (domain layer).
     * passwordHash is intentionally excluded from the domain model.
     */
    private fun UserEntity.toDomainModel(): User {
        return User(
            id              = id,
            fullName        = fullName,
            email           = email,
            messengerHandle = messengerHandle,
            createdAt       = createdAt
        )
    }
}