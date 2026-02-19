package com.campusfind.domain.usecase

import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import com.campusfind.domain.repository.LostItemRepository
import javax.inject.Inject

/**
 * FILE: app/src/main/java/com/campusfind/domain/usecase/UpdateItemStatusUseCase.kt
 *
 * Use Case for updating an item's status (LOST → FOUND).
 * Single Responsibility: enforce ownership and call repository.
 *
 * Why ownership check is here (not in ViewModel):
 * - Business rule enforcement belongs in the domain layer
 * - ViewModel could be bypassed by future code changes
 * - Use case is the single source of truth for this rule
 *
 * Why it receives the full LostItem (not just ID):
 * - Needs item.reportedBy to check ownership
 * - Avoids an extra database query
 *
 * See: DEC-001 (MVVM), DEC-021 (ownership enforcement), TASK-114, demo step 14
 */
class UpdateItemStatusUseCase @Inject constructor(
    private val repository: LostItemRepository,
    private val sessionManager: SessionManager
) {

    /**
     * Update an item's status to FOUND.
     *
     * @param item The item to update (includes reportedBy for ownership check)
     *
     * @return Result.success(Unit) if status updated successfully
     *         Result.failure(exception) if not authorized or update fails
     *
     * Used by: DetailViewModel.onMarkAsFound()
     * Demo step: 14 — User A marks their item as Found
     */
    suspend operator fun invoke(item: LostItem): Result<Unit> {
        // Ownership check
        val currentUserId = sessionManager.currentUserId
            ?: return Result.failure(Exception("You must be logged in"))

        if (item.reportedBy != currentUserId) {
            return Result.failure(Exception("You can only update your own items"))
        }

        // Delegate to repository
        return try {
            repository.updateStatus(item.id, ItemStatus.FOUND)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}