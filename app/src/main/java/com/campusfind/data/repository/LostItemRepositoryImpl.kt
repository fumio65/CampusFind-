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
 * Why @Inject constructor:
 * - Tells Hilt it can construct this class automatically by providing LostItemDao
 * - LostItemDao is provided by DatabaseModule (TASK-100a)
 * - No manual construction needed — Hilt wires everything at compile time
 *
 * Why @Singleton:
 * - Repository should be shared across the entire app — one instance for consistency
 * - Multiple instances could cause race conditions on database writes
 *
 * Why .map { list -> list.map { it.toDomain() } }:
 * - Room returns Flow<List<LostItemEntity>>
 * - ViewModels need Flow<List<LostItem>> (domain model)
 * - Flow.map() transforms each emitted list from entities to domain models
 * - This transformation happens every time Room emits a new list
 *
 * Why entity ↔ domain mapping functions:
 * - LostItemEntity has Room annotations (@Entity, @ColumnInfo)
 * - LostItem is pure Kotlin with no database concerns
 * - Mapping keeps the layers separated (DIP)
 *
 * See: DEC-002 (Repository), DEC-009 (UUID), DEC-010 (enum storage),
 *      DEC-011 (timestamps), DEC-022 (Hilt DI), TASK-100c (bound by RepositoryModule), TASK-111
 */
@Singleton
class LostItemRepositoryImpl @Inject constructor(
    private val lostItemDao: LostItemDao
) : LostItemRepository {

    // ── READ (Flow — reactive) ───────────────────────────────────────────────

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

    // ── READ (suspend — one-shot) ────────────────────────────────────────────

    override suspend fun getItemById(id: String): LostItem? {
        return lostItemDao.getItemById(id)?.toDomain()
    }

    // ── WRITE ────────────────────────────────────────────────────────────────

    override suspend fun addItem(title: String, description: String, reportedBy: String) {
        val timestamp = System.currentTimeMillis()
        val entity = LostItemEntity(
            id              = UUID.randomUUID().toString(),
            title           = title,
            description     = description,
            status          = ItemStatus.LOST.name,    // new items are always LOST
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

    // ── ENTITY ↔ DOMAIN MAPPING ──────────────────────────────────────────────

    /**
     * Convert LostItemEntity (data layer) to LostItem (domain layer).
     *
     * Why extension function:
     * - Keeps mapping logic close to where it's used
     * - Private — only this repository can do the conversion
     * - Cleaner than a standalone mapper class for simple conversions
     */
    private fun LostItemEntity.toDomain(): LostItem {
        return LostItem(
            id              = id,
            title           = title,
            description     = description,
            status          = ItemStatus.fromString(status),  // String → enum
            reportedBy      = reportedBy,
            reportedAt      = reportedAt,
            lastModifiedAt  = lastModifiedAt
        )
    }
}