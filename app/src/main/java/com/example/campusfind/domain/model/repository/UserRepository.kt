package com.campusfind.domain.repository

import com.campusfind.domain.model.User

/**
 * FILE: app/src/main/java/com/campusfind/domain/model/repository/UserRepository.kt
 *
 * Repository interface for user authentication and user data access.
 * Lives in domain/ — pure Kotlin, no Room, no Android imports.
 *
 * Why this is an interface:
 * - DIP — ViewModels and UseCases depend on this abstraction, not on the implementation
 * - Phase 1 uses UserRepositoryImpl (Room-based local auth with SHA-256)
 * - Phase 2 can swap to FirebaseUserRepositoryImpl (Firebase Auth) with zero changes
 *   to LoginUseCase, RegisterUseCase, or any ViewModel
 * - RepositoryModule @Binds wires the interface to the implementation (TASK-100c)
 *
 * Why Result<User> instead of User:
 * - register() and login() can fail — duplicate email, wrong password, etc.
 * - Result.success(user) on success, Result.failure(exception) on error
 * - UseCases unwrap the Result and pass clean success/error to ViewModels
 *
 * See: DEC-002 (Repository Pattern), DEC-016 (local auth), DEC-022 (DIP),
 *      TASK-100c (RepositoryModule), TASK-106
 */
interface UserRepository {

    /**
     * Register a new user account.
     *
     * @param fullName         User's full name (required)
     * @param email            University email (unique, required)
     * @param password         Plain text password (hashed before storage)
     * @param messengerHandle  Optional Facebook Messenger username (nullable)
     *
     * @return Result.success(User) if account created successfully
     *         Result.failure(exception) if email already exists or validation fails
     *
     * Used by: RegisterUseCase → RegisterViewModel
     */
    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        messengerHandle: String?
    ): Result<User>

    /**
     * Log in an existing user.
     *
     * @param email    The email address used at registration
     * @param password Plain text password (compared against stored hash)
     *
     * @return Result.success(User) if credentials are valid
     *         Result.failure(exception) if email not found or password incorrect
     *
     * Used by: LoginUseCase → LoginViewModel
     */
    suspend fun login(
        email: String,
        password: String
    ): Result<User>

    /**
     * Get a user by their ID.
     *
     * @param id User's UUID (from UserEntity.id)
     *
     * @return User if found, null if not found
     *
     * Used by: DetailViewModel to load the reporter's full name for display
     */
    suspend fun getUserById(id: String): User?
}