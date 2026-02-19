package com.campusfind.domain.usecase

import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.repository.LostItemRepository
import javax.inject.Inject

/**
 * FILE: app/src/main/java/com/campusfind/domain/usecase/AddItemUseCase.kt
 *
 * Use Case for creating a lost item report.
 *
 * UPDATED: Now accepts location and photoUri parameters.
 *
 * Validation rules:
 * - Title is required (not blank)
 * - Description is required (not blank)
 * - Location is optional (nullable)
 * - PhotoUri is optional (nullable)
 * - reportedBy must exist (user must be logged in)
 *
 * See: DEC-001 (MVVM), DEC-022 (Hilt DI), TASK-111, TASK-113
 */
class AddItemUseCase @Inject constructor(
    private val repository: LostItemRepository,
    private val sessionManager: SessionManager
) {

    suspend operator fun invoke(
        title: String,
        description: String,
        location: String?,
        photoUri: String?
    ): Result<Unit> {
        // Validation
        if (title.isBlank()) {
            return Result.failure(Exception("Title is required"))
        }
        if (description.isBlank()) {
            return Result.failure(Exception("Description is required"))
        }

        // Get current user ID from session
        val userId = sessionManager.currentUserId
            ?: return Result.failure(Exception("You must be logged in to report an item"))

        // Delegate to repository
        return try {
            repository.addItem(
                title = title.trim(),
                description = description.trim(),
                location = location?.trim()?.takeIf { it.isNotBlank() },
                photoUri = photoUri,
                reportedBy = userId
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}