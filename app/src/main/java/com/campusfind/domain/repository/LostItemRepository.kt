package com.campusfind.domain.repository

import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import kotlinx.coroutines.flow.Flow

/**
 * FILE: app/src/main/java/com/campusfind/domain/repository/LostItemRepository.kt
 *
 * Repository interface for lost/found item data access.
 *
 * UPDATED: Added updateItemDetails() for EditItemViewModel
 *
 * See: DEC-002 (Repository Pattern), DEC-003 (offline-first),
 *      DEC-022 (DIP), TASK-100c (RepositoryModule), TASK-111, TASK-113
 */
interface LostItemRepository {

    fun getAllItems(): Flow<List<LostItem>>

    fun getItemsByStatus(status: ItemStatus): Flow<List<LostItem>>

    suspend fun getItemById(id: String): LostItem?

    /**
     * Create a new lost item report.
     *
     * @param title       Item title (e.g., "Black Wallet")
     * @param description Item description (e.g., "Lost near library entrance")
     * @param location    Where the item was lost/found (nullable)
     * @param photoUri    URI of the photo (nullable)
     * @param reportedBy  userId of the reporter (from SessionManager.currentUserId)
     *
     * Used by: AddItemViewModel via AddItemUseCase
     */
    suspend fun addItem(
        title: String,
        description: String,
        location: String?,
        photoUri: String?,
        reportedBy: String
    )

    suspend fun updateStatus(id: String, status: ItemStatus)

    suspend fun deleteItem(id: String)

    /**
     * Update an existing item's title and description.
     * Used by: EditItemViewModel
     */
    suspend fun updateItemDetails(
        id: String,
        title: String,
        description: String
    )
}