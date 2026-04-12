package com.campusfind.domain.repository

import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import kotlinx.coroutines.flow.Flow

interface LostItemRepository {
    fun getAllItems(): Flow<List<LostItem>>
    fun getItemsByStatus(status: ItemStatus): Flow<List<LostItem>>
    fun getItemsByUser(userId: String): Flow<List<LostItem>>

    // FIX: New Flow-based single-item observer used by DetailViewModel.
    // The old getItemById() was a one-shot suspend fun — it returned once
    // and stopped. This means DetailScreen never updated when SyncWorker
    // wrote new data to Room (new photo URL, status change, etc.).
    // observeItemById() returns a Flow<LostItem?> that stays alive and emits
    // every time Room updates that row — no app restart needed.
    fun observeItemById(id: String): Flow<LostItem?>

    // Kept for EditItemViewModel which only needs the current value once
    suspend fun getItemById(id: String): LostItem?

    suspend fun addItem(
        title: String,
        description: String,
        location: String? = null,
        photoUri: String? = null
    ): Result<Unit>

    suspend fun updateItemDetails(
        id: String,
        title: String,
        description: String,
        location: String? = null,
        photoUri: String? = null,
        photoChanged: Boolean = false
    ): Result<Unit>

    suspend fun updateItemStatus(id: String, status: ItemStatus): Result<Unit>
    suspend fun deleteItem(id: String): Result<Unit>
}