package com.campusfind.domain.usecase

import com.campusfind.domain.model.User
import com.campusfind.domain.repository.UserRepository
import javax.inject.Inject

/**
 * RegisterUseCase - UPDATED
 *
 * Single responsibility: Register a new user
 *
 * UPDATED: Messenger username is now REQUIRED (not optional)
 * - Validates that messengerHandle is not null or blank
 * - Returns error if messenger username is missing
 * - Ensures all users can coordinate item returns via Messenger
 *
 * Validation rules (enforced in this order):
 * 1. Full name must not be blank
 * 2. Email must not be blank and must contain @
 * 3. Password must be at least 8 characters
 * 4. Password and confirm password must match
 * 5. Messenger username must not be blank (REQUIRED) ✅ NEW
 *
 * Why UseCase instead of putting logic in ViewModel:
 * - Single Responsibility Principle (SRP) — ViewModel handles UI state, UseCase handles business logic
 * - Reusability — same logic can be used from different ViewModels or screens
 * - Testability — easy to unit test without Android dependencies
 *
 * Why @Inject constructor:
 * - Hilt injects UserRepository automatically
 * - No manual dependency creation
 * - Satisfies Dependency Inversion Principle (DIP) — depends on interface, not implementation
 *
 * @param fullName User's full name (e.g. "Juan dela Cruz")
 * @param email University email (must contain @)
 * @param password Plain text password (min 8 chars) — will be hashed in repository
 * @param confirmPassword Must match password
 * @param messengerHandle Messenger username (REQUIRED) — stored without @ prefix
 *
 * @return Result<User> — Success with User domain model, or Failure with exception message
 */
class RegisterUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        messengerHandle: String?  // Still nullable in signature for backward compatibility
    ): Result<User> {
        // ── VALIDATION ────────────────────────────────────────────────

        // 1. Full name validation
        if (fullName.isBlank()) {
            return Result.failure(Exception("Full name is required"))
        }

        if (fullName.length < 2) {
            return Result.failure(Exception("Full name must be at least 2 characters"))
        }

        // 2. Email validation
        if (email.isBlank()) {
            return Result.failure(Exception("Email is required"))
        }

        if (!email.contains("@")) {
            return Result.failure(Exception("Please enter a valid email address"))
        }

        // 3. Password validation
        if (password.isBlank()) {
            return Result.failure(Exception("Password is required"))
        }

        if (password.length < 8) {
            return Result.failure(Exception("Password must be at least 8 characters"))
        }

        // 4. Confirm password validation
        if (password != confirmPassword) {
            return Result.failure(Exception("Passwords do not match"))
        }

        // 5. ✅ NEW: Messenger username validation (REQUIRED)
        if (messengerHandle.isNullOrBlank()) {
            return Result.failure(
                Exception("Messenger username is required to coordinate item returns with finders")
            )
        }

        if (messengerHandle.length < 2) {
            return Result.failure(Exception("Messenger username must be at least 2 characters"))
        }

        // ── REPOSITORY CALL ───────────────────────────────────────────

        // All validation passed — delegate to repository
        // Repository will:
        // 1. Check if email already exists
        // 2. Hash the password (SHA-256)
        // 3. Insert user into Room database
        // 4. Return User domain model or failure
        return userRepository.register(
            fullName = fullName.trim(),
            email = email.trim().lowercase(),
            password = password,
            messengerHandle = messengerHandle.trim()  // ✅ Now guaranteed to be non-null
        )
    }
}