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

    override suspend fun getItemById(id: String): LostItem? =
        dao.getItemById(id)?.toDomain()

    override fun getItemsByUser(userId: String): Flow<List<LostItem>> =
        dao.getItemsByUser(userId).map { list -> list.map { it.toDomain() } }

    // ✅ UPDATED: Now accepts location parameter
    override suspend fun addItem(
        title: String,
        description: String,
        location: String?,
        photoUri: String?
    ): Result<Unit> {
        return try {
            val currentUserId = sessionManager.currentUserId
                ?: return Result.failure(Exception("Not logged in"))

            // Convert content:// URI to a stable internal file path.
            // content:// URIs become invalid after the picker is dismissed;
            // PhotoManager copies the bytes to app internal storage.
            val stablePhotoUri = if (photoUri?.startsWith("content://") == true) {
                photoManager.savePhoto(Uri.parse(photoUri))
            } else {
                photoUri  // already a file path or null
            }

            val entity = LostItemEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                location = location,
                status = "LOST",
                reportedBy = currentUserId,
                reportedAt = System.currentTimeMillis(),
                lastModifiedAt = System.currentTimeMillis(),
                photoUri = stablePhotoUri,
                syncStatus = SyncStatus.PENDING_SYNC.name
            )
            dao.insertItem(entity)
            syncManager.triggerNow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ UPDATED: Now accepts location parameter
    override suspend fun updateItemDetails(
        id: String,
        title: String,
        description: String,
        location: String?
    ): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            dao.updateItemDetailsWithLocation(id, title, description, location, timestamp)
            dao.updateSyncStatus(id, SyncStatus.PENDING_SYNC.name)
            syncManager.triggerNow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateItemStatus(id: String, status: ItemStatus): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            dao.updateItemStatus(id, status.name, timestamp)
            dao.updateSyncStatus(id, SyncStatus.PENDING_SYNC.name)
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

    // ✅ UPDATED: Mapping includes location
    private fun LostItemEntity.toDomain() = LostItem(
        id = id,
        title = title,
        description = description,
        location = location,  // ✅ NEW
        status = ItemStatus.valueOf(status),
        reportedBy = reportedBy,
        reportedAt = reportedAt,
        lastModifiedAt = lastModifiedAt,
        photoUri = photoUri
    )
}