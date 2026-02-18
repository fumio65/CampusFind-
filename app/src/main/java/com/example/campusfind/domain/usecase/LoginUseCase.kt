package com.campusfind.domain.usecase

import com.campusfind.domain.model.User
import com.campusfind.domain.repository.UserRepository
import javax.inject.Inject

/**
 * FILE: app/src/main/java/com/campusfind/domain/usecase/LoginUseCase.kt
 *
 * Use Case for user login.
 * Single Responsibility: validate login input and call the repository.
 *
 * Why this exists separate from LoginViewModel:
 * - SRP — ViewModel manages UI state, UseCase handles business logic
 * - ViewModel stays thin and focused on Compose state management
 * - UseCase is pure Kotlin — testable without Android dependencies
 * - Reusable across multiple ViewModels if needed (though typically 1:1)
 *
 * Why operator fun invoke:
 * - Allows calling the use case like a function: loginUseCase(email, password)
 * - More readable than loginUseCase.execute(email, password)
 * - Standard Kotlin convention for single-method use cases
 *
 * Why @Inject constructor:
 * - Hilt provides UserRepository automatically (bound by RepositoryModule)
 * - No manual construction — Hilt wires the entire dependency graph
 *
 * See: DEC-001 (MVVM), DEC-002 (Repository), DEC-022 (Hilt DI),
 *      TASK-106, TASK-108 (LoginViewModel)
 */
class LoginUseCase @Inject constructor(
    private val repository: UserRepository   // interface — DIP satisfied
) {

    /**
     * Attempt to log in a user with email and password.
     *
     * Validation:
     * - Email cannot be blank
     * - Password cannot be blank
     *
     * @param email    User's email address
     * @param password User's plain text password
     *
     * @return Result.success(User) if credentials are valid
     *         Result.failure(exception) if validation fails or credentials are incorrect
     *
     * Used by: LoginViewModel.onLoginClicked()
     */
    suspend operator fun invoke(email: String, password: String): Result<User> {
        // Validation
        if (email.isBlank()) {
            return Result.failure(Exception("Email is required"))
        }
        if (password.isBlank()) {
            return Result.failure(Exception("Password is required"))
        }

        // Delegate to repository
        return repository.login(email.trim(), password)
    }
}