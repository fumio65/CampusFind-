package com.campusfind.domain.repository

import com.campusfind.domain.model.User

/**
 * UserRepository - COMPLETE WITH ALL METHODS
 *
 * Interface for user-related operations
 */
interface UserRepository {
    // ══════════════════════════════════════
    // AUTH METHODS
    // ══════════════════════════════════════

    /**
     * Register a new user
     */
    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        messengerHandle: String?
    ): Result<User>

    /**
     * Login with email and password
     */
    suspend fun login(
        email: String,
        password: String
    ): Result<User>

    // ══════════════════════════════════════
    // USER QUERY METHODS
    // ══════════════════════════════════════

    /**
     * Get user by ID (for profile screen, reporter name, etc.)
     */
    suspend fun getUserById(id: String): User?

    // ══════════════════════════════════════
    // USER UPDATE METHODS
    // ══════════════════════════════════════

    /**
     * Update user's Messenger handle (Add/Edit from UserProfileScreen)
     */
    suspend fun updateMessengerHandle(
        userId: String,
        messengerHandle: String
    ): Result<Unit>
}