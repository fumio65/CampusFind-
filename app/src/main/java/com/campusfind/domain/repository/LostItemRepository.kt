package com.campusfind.domain.repository

import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import kotlinx.coroutines.flow.Flow

interface LostItemRepository {
    fun getAllItems(): Flow<List<LostItem>>
    fun getItemsByStatus(status: ItemStatus): Flow<List<LostItem>>
    suspend fun getItemById(id: String): LostItem?
    fun getItemsByUser(userId: String): Flow<List<LostItem>>

    // Returns item ID on success so caller can update photoUri after upload
    suspend fun addItem(
        title: String,
        description: String,
        location: String? = null,
        photoUri: String? = null
    ): Result<String>   // ← changed from Result<Unit> to Result<String> (itemId)

    suspend fun updateItemDetails(
        id: String,
        title: String,
        description: String,
        location: String? = null
    ): Result<Unit>

    suspend fun updateItemStatus(id: String, status: ItemStatus): Result<Unit>
    suspend fun updatePhotoUri(id: String, photoUri: String): Result<Unit>  // ← NEW
    suspend fun deleteItem(id: String): Result<Unit>
}