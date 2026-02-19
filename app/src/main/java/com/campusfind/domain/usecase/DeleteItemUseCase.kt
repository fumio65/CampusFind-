package com.campusfind.domain.usecase

import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.model.LostItem
import com.campusfind.domain.repository.LostItemRepository
import javax.inject.Inject

/**
 * FILE: app/src/main/java/com/campusfind/domain/usecase/DeleteItemUseCase.kt
 *
 * Use Case for deleting an item.
 * Single Responsibility: enforce ownership and call repository.
 *
 * Why ownership check is here:
 * - Same reason as UpdateItemStatusUseCase
 * - Business rule: only the reporter can delete their own item
 * - Enforced at the domain layer, not just in UI
 *
 * See: DEC-001 (MVVM), DEC-021 (ownership enforcement), TASK-114, TASK-116
 */
class DeleteItemUseCase @Inject constructor(
    private val repository: LostItemRepository,
    private val sessionManager: SessionManager
) {

    /**
     * Delete an item permanently.
     *
     * @param item The item to delete (includes reportedBy for ownership check)
     *
     * @return Result.success(Unit) if item deleted successfully
     *         Result.failure(exception) if not authorized or delete fails
     *
     * Used by: DetailViewModel.onDeleteConfirmed()
     */
    suspend operator fun invoke(item: LostItem): Result<Unit> {
        // Ownership check
        val currentUserId = sessionManager.currentUserId
            ?: return Result.failure(Exception("You must be logged in"))

        if (item.reportedBy != currentUserId) {
            return Result.failure(Exception("You can only delete your own items"))
        }

        // Delegate to repository
        return try {
            repository.deleteItem(item.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}