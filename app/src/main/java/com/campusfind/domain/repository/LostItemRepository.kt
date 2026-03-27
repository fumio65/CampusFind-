package com.campusfind.domain.repository

import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import kotlinx.coroutines.flow.Flow

interface LostItemRepository {
    fun getAllItems(): Flow<List<LostItem>>
    fun getItemsByStatus(status: ItemStatus): Flow<List<LostItem>>
    suspend fun getItemById(id: String): LostItem?
    fun getItemsByUser(userId: String): Flow<List<LostItem>>

    // ✅ UPDATED: Now includes location parameter
    suspend fun addItem(
        title: String,
        description: String,
        location: String? = null,  // NEW
        photoUri: String? = null
    ): Result<Unit>

    suspend fun updateItemDetails(
        id: String,
        title: String,
        description: String,
        location: String? = null  // NEW
    ): Result<Unit>

    suspend fun updateItemStatus(id: String, status: ItemStatus): Result<Unit>
    suspend fun deleteItem(id: String): Result<Unit>
}