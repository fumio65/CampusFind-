package com.campusfind.data.repository

import com.campusfind.data.local.database.UserDao
import com.campusfind.data.local.database.UserEntity
import com.campusfind.domain.model.User
import com.campusfind.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

/**
 * FirebaseAuthRepositoryImpl.kt
 *
 * Replaces UserRepositoryImpl (SHA-256 local auth) with Firebase Auth.
 *
 * Strategy:
 * - Firebase Auth handles login/register — proper secure auth
 * - User profile (fullName, messengerHandle) stored in BOTH:
 *   1. Room `users` table — for offline access + local queries
 *   2. Firestore `/users/{uid}` — for cross-device access
 * - On login, Room is updated from Firestore if user data is missing locally
 *
 * Migration from local auth:
 * - Existing local users (SHA-256) can re-register with same email/password
 * - Firebase Auth treats it as a new account — Room keeps same UUID pattern
 *   but now uses Firebase UID as the primary key
 *
 * See: DEC-016 (auth migration), DEC-007 (Firebase)
 */
class FirebaseAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : UserRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    // ── Register ───────────────────────────────────────────────────────────

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        messengerHandle: String?
    ): Result<User> {
        return try {
            // 1. Create Firebase Auth account
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Registration failed"))

            val uid = firebaseUser.uid

            // 2. Build user profile
            val user = User(
                id              = uid,       // Firebase UID as primary key
                fullName        = fullName,
                email           = email,
                messengerHandle = messengerHandle,
                createdAt       = System.currentTimeMillis()
            )

            // 3. Save to Firestore /users/{uid}
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .set(user.toFirestoreMap())
                .await()

            // 4. Save to local Room for offline access
            userDao.insertUser(user.toEntity())

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    // ── Login ──────────────────────────────────────────────────────────────

    override suspend fun login(
        email: String,
        password: String
    ): Result<User> {
        return try {
            // 1. Sign in with Firebase Auth
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Login failed"))

            val uid = firebaseUser.uid

            // 2. Try to get user profile from local Room first (offline-first)
            val localUser = userDao.getUserById(uid)
            if (localUser != null) {
                return Result.success(localUser.toDomain())
            }

            // 3. Not in Room — fetch from Firestore and cache locally
            val doc = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            val user = doc.toUser()
                ?: return Result.failure(Exception("User profile not found"))

            // Cache in Room for future offline access
            userDao.insertUser(user.toEntity())

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    // ── Get user by ID ─────────────────────────────────────────────────────

    override suspend fun getUserById(id: String): User? {
        // Room first (offline-first)
        val local = userDao.getUserById(id)
        if (local != null) return local.toDomain()

        // Fallback to Firestore
        return try {
            val doc = firestore.collection(USERS_COLLECTION)
                .document(id)
                .get()
                .await()
            doc.toUser()
        } catch (e: Exception) {
            null
        }
    }

    // ── Update Messenger Handle ────────────────────────────────────────────

    override suspend fun updateMessengerHandle(
        userId: String,
        messengerHandle: String
    ): Result<Unit> {
        return try {
            // Update both Room and Firestore
            userDao.updateMessengerHandle(userId, messengerHandle)

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update("messengerHandle", messengerHandle)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Sign out ───────────────────────────────────────────────────────────

    fun signOut() {
        firebaseAuth.signOut()
    }

    // ── Current Firebase user ──────────────────────────────────────────────

    fun getCurrentFirebaseUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    // ── Mapping helpers ────────────────────────────────────────────────────

    private fun User.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id"              to id,
        "fullName"        to fullName,
        "email"           to email,
        "messengerHandle" to messengerHandle,
        "createdAt"       to createdAt
    )

    private fun User.toEntity() = UserEntity(
        id              = id,
        fullName        = fullName,
        email           = email,
        passwordHash    = "",    // Firebase Auth handles passwords — not stored locally
        messengerHandle = messengerHandle,
        createdAt       = createdAt
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User? {
        return try {
            User(
                id              = getString("id") ?: id,
                fullName        = getString("fullName") ?: return null,
                email           = getString("email") ?: return null,
                messengerHandle = getString("messengerHandle"),
                createdAt       = getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun UserEntity.toDomain() = User(
        id              = id,
        fullName        = fullName,
        email           = email,
        messengerHandle = messengerHandle,
        createdAt       = createdAt
    )

    // ── Map Firebase error messages to user-friendly ones ─────────────────

    private fun mapFirebaseException(e: Exception): Exception {
        val msg = e.message ?: "An error occurred"
        return when {
            msg.contains("email address is already in use") ->
                Exception("This email is already registered")
            msg.contains("no user record") ||
                    msg.contains("user-not-found") ->
                Exception("No account found with this email")
            msg.contains("password is invalid") ||
                    msg.contains("wrong-password") ->
                Exception("Incorrect password")
            msg.contains("badly formatted") ->
                Exception("Invalid email address")
            msg.contains("weak-password") ->
                Exception("Password must be at least 6 characters")
            msg.contains("network") ||
                    msg.contains("NETWORK") ->
                Exception("No internet connection. Please try again.")
            else -> Exception(msg)
        }
    }
}