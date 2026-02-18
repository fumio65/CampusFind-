package com.campusfind.domain.repository

import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import kotlinx.coroutines.flow.Flow

/**
 * FILE: app/src/main/java/com/campusfind/domain/repository/LostItemRepository.kt
 *
 * Repository interface for lost/found item data access.
 * Lives in domain/ — pure Kotlin, no Room, no Android imports.
 *
 * Why this is an interface:
 * - DIP — ViewModels and UseCases depend on this abstraction, not on the implementation
 * - Phase 1 uses LostItemRepositoryImpl (Room-based local storage)
 * - Phase 2 swaps to CloudSyncLostItemRepositoryImpl (Room + Firestore sync)
 *   with zero changes to any ViewModel or UseCase
 * - RepositoryModule @Binds wires the interface to the implementation (TASK-100c)
 *
 * Why some methods return Flow and others are suspend:
 * - Flow  → used for lists that need to auto-update the UI in real time
 *           When Room data changes, Flow emits a new list automatically
 * - suspend → used for one-shot operations (add, update, delete)
 *             and single-item reads that do not need to be observed
 *
 * See: DEC-002 (Repository Pattern), DEC-003 (offline-first),
 *      DEC-022 (DIP), TASK-100c (RepositoryModule), TASK-111
 */
interface LostItemRepository {

    /**
     * Get all items regardless of status.
     * Returns a Flow that emits a new list whenever Room data changes.
     *
     * Used by: HomeViewModel when filter = null (All tab)
     * Supports: Demo step 9 — User B sees User A's items from the same database
     */
    fun getAllItems(): Flow<List<LostItem>>

    /**
     * Get items filtered by status (LOST or FOUND).
     * Returns a Flow that emits a new list whenever Room data changes.
     *
     * Used by: HomeViewModel when filter chip = LOST or FOUND
     */
    fun getItemsByStatus(status: ItemStatus): Flow<List<LostItem>>

    /**
     * Get a single item by its ID.
     * Returns null if the item doesn't exist or was deleted.
     *
     * Used by: DetailViewModel to load the item when user taps an ItemCard
     */
    suspend fun getItemById(id: String): LostItem?

    /**
     * Create a new lost item report.
     *
     * @param title       Item title (e.g., "Black Wallet")
     * @param description Item description (e.g., "Lost near library entrance")
     * @param reportedBy  userId of the reporter (from SessionManager.currentUserId)
     *
     * Used by: AddItemViewModel via AddItemUseCase
     * Demo step: 4 — User A creates a report
     */
    suspend fun addItem(title: String, description: String, reportedBy: String)

    /**
     * Update an item's status (LOST → FOUND).
     *
     * @param id     Item's UUID
     * @param status New status (typically ItemStatus.FOUND)
     *
     * Used by: DetailViewModel via UpdateItemStatusUseCase
     * Demo step: 14 — User A marks their item as Found
     */
    suspend fun updateStatus(id: String, status: ItemStatus)

    /**
     * Delete an item permanently.
     *
     * @param id Item's UUID
     *
     * Used by: DetailViewModel via DeleteItemUseCase (overflow menu)
     * Ownership: Only the reporter can delete their own item (enforced in UseCase)
     */
    suspend fun deleteItem(id: String)
}