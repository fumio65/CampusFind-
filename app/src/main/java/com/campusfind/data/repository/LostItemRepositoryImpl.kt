package com.campusfind.data.repository

import android.net.Uri
import com.campusfind.data.local.database.LostItemDao
import com.campusfind.data.local.database.LostItemEntity
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.data.local.photo.PhotoManager
import com.campusfind.data.sync.SyncManager
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import com.campusfind.domain.model.SyncStatus
import com.campusfind.domain.repository.LostItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class LostItemRepositoryImpl @Inject constructor(
    private val dao: LostItemDao,
    private val sessionManager: SessionManager,
    private val syncManager: SyncManager,
    private val photoManager: PhotoManager
) : LostItemRepository {

    override fun getAllItems(): Flow<List<LostItem>> =
        dao.getAllItems().map { list -> list.map { it.toDomain() } }

    override fun getItemsByStatus(status: ItemStatus): Flow<List<LostItem>> =
        dao.getItemsByStatus(status.name).map { list -> list.map { it.toDomain() } }

    override fun getItemsByUser(userId: String): Flow<List<LostItem>> =
        dao.getItemsByUser(userId).map { list -> list.map { it.toDomain() } }

    // Continuous Flow observer — used by DetailViewModel so DetailScreen
    // updates live whenever Room changes (sync, edit, status change, etc.)
    override fun observeItemById(id: String): Flow<LostItem?> =
        dao.observeItemById(id).map { it?.toDomain() }

    // One-shot fetch — used by EditItemViewModel which only needs the
    // current value once to pre-fill the form
    override suspend fun getItemById(id: String): LostItem? =
        dao.getItemById(id)?.toDomain()

    override suspend fun addItem(
        title: String,
        description: String,
        location: String?,
        photoUri: String?
    ): Result<Unit> {
        return try {
            val currentUserId = sessionManager.currentUserId
                ?: return Result.failure(Exception("Not logged in"))

            val stablePhotoUri = if (photoUri?.startsWith("content://") == true) {
                photoManager.savePhoto(Uri.parse(photoUri))
            } else {
                photoUri
            }

            val entity = LostItemEntity(
                id             = UUID.randomUUID().toString(),
                title          = title,
                description    = description,
                location       = location,
                status         = "LOST",
                reportedBy     = currentUserId,
                reportedAt     = System.currentTimeMillis(),
                lastModifiedAt = System.currentTimeMillis(),
                photoUri       = stablePhotoUri,
                syncStatus     = SyncStatus.PENDING_SYNC.name
            )
            dao.insertItem(entity)
            syncManager.triggerNow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateItemDetails(
        id: String,
        title: String,
        description: String,
        location: String?,
        photoUri: String?,
        photoChanged: Boolean
    ): Result<Unit> {
        return try {
            val finalPhotoUri: String? = when {
                photoChanged && photoUri != null -> photoUri
                photoChanged && photoUri == null -> null
                else -> photoUri
            }
            dao.updateItemFull(
                id          = id,
                title       = title,
                description = description,
                location    = location,
                timestamp   = System.currentTimeMillis(),
                photoUri    = finalPhotoUri
            )
            syncManager.triggerNow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateItemStatus(id: String, status: ItemStatus): Result<Unit> {
        return try {
            dao.updateItemStatus(id, status.name, System.currentTimeMillis())
            syncManager.triggerNow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteItem(id: String): Result<Unit> {
        return try {
            dao.deleteItem(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun LostItemEntity.toDomain() = LostItem(
        id             = id,
        title          = title,
        description    = description,
        location       = location,
        status         = ItemStatus.valueOf(status),
        reportedBy     = reportedBy,
        reportedAt     = reportedAt,
        lastModifiedAt = lastModifiedAt,
        photoUri       = photoUri
    )
}