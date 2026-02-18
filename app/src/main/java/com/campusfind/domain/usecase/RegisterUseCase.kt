package com.campusfind.domain.usecase

import com.campusfind.domain.model.User
import com.campusfind.domain.repository.UserRepository
import javax.inject.Inject

/**
 * FILE: app/src/main/java/com/campusfind/domain/usecase/RegisterUseCase.kt
 *
 * Use Case for user registration.
 * Single Responsibility: validate registration input and call the repository.
 *
 * Why this exists separate from RegisterViewModel:
 * - SRP — ViewModel manages UI state, UseCase handles business logic and validation
 * - All validation rules are centralized here, not scattered across the ViewModel
 * - Pure Kotlin — testable without Android or Compose dependencies
 *
 * Why operator fun invoke:
 * - Allows calling the use case like a function: registerUseCase(name, email, password, ...)
 * - Standard Kotlin convention for single-method use cases
 *
 * Validation rules enforced here:
 * - Full name is required (not blank)
 * - Email is required and must contain '@' (basic format check)
 * - Password is required and must be at least 6 characters
 * - Messenger handle is optional (nullable)
 *
 * Why @Inject constructor:
 * - Hilt provides UserRepository automatically (bound by RepositoryModule)
 *
 * See: DEC-001 (MVVM), DEC-002 (Repository), DEC-022 (Hilt DI),
 *      TASK-106, TASK-107 (RegisterViewModel)
 */
class RegisterUseCase @Inject constructor(
    private val repository: UserRepository   // interface — DIP satisfied
) {

    /**
     * Attempt to register a new user account.
     *
     * Validation:
     * - Full name is required (not blank)
     * - Email is required and must contain '@'
     * - Password is required and must be at least 6 characters
     * - Messenger handle is optional (nullable)
     *
     * @param fullName         User's full name (required)
     * @param email            University email (required, must contain '@')
     * @param password         Plain text password (required, min 6 chars)
     * @param messengerHandle  Optional Messenger username (nullable)
     *
     * @return Result.success(User) if account created successfully
     *         Result.failure(exception) if validation fails or email already exists
     *
     * Used by: RegisterViewModel.onSubmit()
     */
    suspend operator fun invoke(
        fullName: String,
        email: String,
        password: String,
        messengerHandle: String?
    ): Result<User> {

        // Validation — fail fast on first error
        if (fullName.isBlank()) {
            return Result.failure(Exception("Full name is required"))
        }
        if (email.isBlank()) {
            return Result.failure(Exception("Email is required"))
        }
        if (!email.contains("@")) {
            return Result.failure(Exception("Email must be a valid email address"))
        }
        if (password.isBlank()) {
            return Result.failure(Exception("Password is required"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }

        // Delegate to repository
        return repository.register(
            fullName.trim(),
            email.trim(),
            password,
            messengerHandle?.trim()?.takeIf { it.isNotBlank() }   // convert empty string to null
        )
    }
}