package com.campusfind.domain.usecase

import com.campusfind.domain.repository.LostItemRepository
import javax.inject.Inject

/**
 * AddItemUseCase - WITH PHOTO SUPPORT
 *
 * Business logic for creating a lost item report
 *
 * Updated to accept photoUri parameter
 */
class AddItemUseCase @Inject constructor(
    private val repository: LostItemRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        photoUri: String? = null  // ✅ NEW: Photo URI parameter (nullable)
    ): Result<Unit> {
        return try {
            // Validation
            if (title.isBlank()) {
                return Result.failure(Exception("Title is required"))
            }
            if (description.isBlank()) {
                return Result.failure(Exception("Description is required"))
            }

            // Save to repository
            repository.addItem(
                title = title.trim(),
                description = description.trim(),
                photoUri = photoUri  // ✅ NEW: Pass photo URI
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}