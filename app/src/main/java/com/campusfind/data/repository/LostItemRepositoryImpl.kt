package com.campusfind.data.repository

import com.campusfind.data.local.database.LostItemDao
import com.campusfind.data.local.database.LostItemEntity
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import com.campusfind.domain.repository.LostItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FILE: app/src/main/java/com/campusfind/data/repository/LostItemRepositoryImpl.kt
 *
 * Implementation of LostItemRepository using Room for local storage.
 *
 * UPDATED: addItem() now saves location and photoUri to Room.
 *
 * See: DEC-002 (Repository), DEC-009 (UUID), DEC-010 (enum storage),
 *      DEC-011 (timestamps), DEC-022 (Hilt DI), TASK-100c, TASK-111, TASK-113
 */
@Singleton
class LostItemRepositoryImpl @Inject constructor(
    private val lostItemDao: LostItemDao
) : LostItemRepository {

    override fun getAllItems(): Flow<List<LostItem>> {
        return lostItemDao.getAllItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getItemsByStatus(status: ItemStatus): Flow<List<LostItem>> {
        return lostItemDao.getItemsByStatus(status.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getItemById(id: String): LostItem? {
        return lostItemDao.getItemById(id)?.toDomain()
    }

    override suspend fun addItem(
        title: String,
        description: String,
        location: String?,
        photoUri: String?,
        reportedBy: String
    ) {
        val timestamp = System.currentTimeMillis()
        val entity = LostItemEntity(
            id              = UUID.randomUUID().toString(),
            title           = title,
            description     = description,
            location        = location,             // NEW
            photoUri        = photoUri,             // NEW
            status          = ItemStatus.LOST.name,
            reportedBy      = reportedBy,
            reportedAt      = timestamp,
            lastModifiedAt  = timestamp
        )
        lostItemDao.insertItem(entity)
    }

    override suspend fun updateStatus(id: String, status: ItemStatus) {
        val timestamp = System.currentTimeMillis()
        lostItemDao.updateItemStatus(id, status.name, timestamp)
    }

    override suspend fun deleteItem(id: String) {
        lostItemDao.deleteItem(id)
    }

    private fun LostItemEntity.toDomain(): LostItem {
        return LostItem(
            id              = id,
            title           = title,
            description     = description,
            location        = location,             // NEW
            photoUri        = photoUri,             // NEW
            status          = ItemStatus.fromString(status),
            reportedBy      = reportedBy,
            reportedAt      = reportedAt,
            lastModifiedAt  = lastModifiedAt
        )
    }
}