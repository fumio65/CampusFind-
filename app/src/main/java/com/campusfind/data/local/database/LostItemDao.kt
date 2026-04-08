package com.campusfind.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LostItemDao {

    @Query("SELECT * FROM lost_items ORDER BY reported_at DESC")
    fun getAllItems(): Flow<List<LostItemEntity>>

    @Query("SELECT * FROM lost_items WHERE status = :status ORDER BY reported_at DESC")
    fun getItemsByStatus(status: String): Flow<List<LostItemEntity>>

    @Query("SELECT * FROM lost_items WHERE id = :id")
    suspend fun getItemById(id: String): LostItemEntity?

    @Query("SELECT * FROM lost_items WHERE reported_by = :userId ORDER BY reported_at DESC")
    fun getItemsByUser(userId: String): Flow<List<LostItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LostItemEntity)

    @Update
    suspend fun updateItem(item: LostItemEntity)

    @Query("""
        UPDATE lost_items 
        SET status = :status, 
            last_modified_at = :timestamp
        WHERE id = :id
    """)
    suspend fun updateItemStatus(id: String, status: String, timestamp: Long)

    // ✅ NEW: Update with location support
    @Query("""
        UPDATE lost_items 
        SET title = :title,
            description = :description,
            location = :location,
            last_modified_at = :timestamp
        WHERE id = :id
    """)
    suspend fun updateItemDetailsWithLocation(
        id: String,
        title: String,
        description: String,
        location: String?,
        timestamp: Long
    )

    @Query("DELETE FROM lost_items WHERE id = :id")
    suspend fun deleteItem(id: String)

    @Query("SELECT * FROM lost_items WHERE sync_status = 'PENDING_SYNC'")
    suspend fun getPendingSyncItems(): List<LostItemEntity>

    @Query("UPDATE lost_items SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)

    // Used for bulk pull from Supabase — overwrites everything (for new devices / User B)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<LostItemEntity>)

    // Smart upsert for pull: insert if new, or update metadata only (preserves local photo_uri)
    @Query("""
        INSERT INTO lost_items (id, title, description, location, status, reported_by, reported_at, last_modified_at, photo_uri, sync_status)
        VALUES (:id, :title, :description, :location, :status, :reportedBy, :reportedAt, :lastModifiedAt, :remotePhotoUri, 'SYNCED')
        ON CONFLICT(id) DO UPDATE SET
            title = excluded.title,
            description = excluded.description,
            location = excluded.location,
            status = excluded.status,
            last_modified_at = excluded.last_modified_at,
            sync_status = 'SYNCED',
            photo_uri = CASE
                WHEN photo_uri LIKE '/%' THEN photo_uri
                ELSE excluded.photo_uri
            END
    """)
    suspend fun upsertFromRemote(
        id: String, title: String, description: String, location: String?,
        status: String, reportedBy: String, reportedAt: Long,
        lastModifiedAt: Long, remotePhotoUri: String?
    )

    @Query("UPDATE lost_items SET photo_uri = :photoUri WHERE id = :id")
    suspend fun updatePhotoUri(id: String, photoUri: String)
}