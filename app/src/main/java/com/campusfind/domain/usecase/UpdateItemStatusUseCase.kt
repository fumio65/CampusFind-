package com.campusfind.domain.usecase

import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import com.campusfind.domain.repository.LostItemRepository
import javax.inject.Inject

/**
 * FILE: app/src/main/java/com/campusfind/domain/usecase/UpdateItemStatusUseCase.kt
 *
 * Use Case for updating an item's status.
 * Single Responsibility: enforce ownership and call repository.
 *
 * FIXED: Now calls repository.updateItemStatus() (correct method name)
 *
 * See: DEC-001 (MVVM), DEC-021 (ownership enforcement), TASK-114
 */
class UpdateItemStatusUseCase @Inject constructor(
    private val repository: LostItemRepository,
    private val sessionManager: SessionManager
) {

    /**
     * Update an item's status to the specified target status.
     *
     * @param item The item to update (includes reportedBy for ownership check)
     * @param targetStatus The new status (LOST or FOUND)
     *
     * @return Result.success(Unit) if status updated successfully
     *         Result.failure(exception) if not authorized or update fails
     *
     * Used by: DetailViewModel when marking as found
     */
    suspend operator fun invoke(item: LostItem, targetStatus: ItemStatus): Result<Unit> {
        // Ownership check
        val currentUserId = sessionManager.currentUserId
            ?: return Result.failure(Exception("You must be logged in"))

        if (item.reportedBy != currentUserId) {
            return Result.failure(Exception("You can only update your own items"))
        }

        // Delegate to repository (FIXED: correct method name)
        return repository.updateItemStatus(item.id, targetStatus)
    }
}