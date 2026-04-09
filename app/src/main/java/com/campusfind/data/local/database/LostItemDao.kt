package com.campusfind.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class LostItemDao {

    // ── Standard queries ─────────────────────────────────────────────────

    @Query("SELECT * FROM lost_items ORDER BY reported_at DESC")
    abstract fun getAllItems(): Flow<List<LostItemEntity>>

    @Query("SELECT * FROM lost_items WHERE status = :status ORDER BY reported_at DESC")
    abstract fun getItemsByStatus(status: String): Flow<List<LostItemEntity>>

    // One-shot fetch — for EditItemViewModel pre-filling the form
    @Query("SELECT * FROM lost_items WHERE id = :id")
    abstract suspend fun getItemById(id: String): LostItemEntity?

    // Continuous Flow — for DetailViewModel live updates.
    // Room re-emits every time this row changes (sync, edit, status update).
    @Query("SELECT * FROM lost_items WHERE id = :id")
    abstract fun observeItemById(id: String): Flow<LostItemEntity?>

    @Query("SELECT * FROM lost_items WHERE reported_by = :userId ORDER BY reported_at DESC")
    abstract fun getItemsByUser(userId: String): Flow<List<LostItemEntity>>

    @Query("SELECT * FROM lost_items WHERE sync_status = 'PENDING_SYNC'")
    abstract suspend fun getPendingSyncItems(): List<LostItemEntity>

    // ── Insert ───────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertItem(item: LostItemEntity)

    // ── Update ───────────────────────────────────────────────────────────

    // FIX: Single query that updates ALL editable fields including photo_uri
    // and sync_status atomically in one Room transaction.
    //
    // The old approach called three separate queries:
    //   1. updateItemDetailsWithLocation  (no photo_uri)
    //   2. updatePhotoUri                 (conditional)
    //   3. updateSyncStatus
    //
    // Between queries 1 and 2, Room emits a Flow update. The UI re-renders
    // with the new title/description but the OLD photo because photo_uri
    // hasn't been written yet. With a single query this cannot happen —
    // Room emits exactly one Flow update after all columns are committed.
    //
    // photoUri is always passed — caller sends the resolved final path
    // (new local path, null, or the unchanged current value). This removes
    // the need for a separate conditional updatePhotoUri call entirely.
    @Query("""
        UPDATE lost_items SET
            title            = :title,
            description      = :description,
            location         = :location,
            last_modified_at = :timestamp,
            photo_uri        = :photoUri,
            sync_status      = 'PENDING_SYNC'
        WHERE id = :id
    """)
    abstract suspend fun updateItemFull(
        id: String,
        title: String,
        description: String,
        location: String?,
        timestamp: Long,
        photoUri: String?
    )

    // Kept for status-only updates (Mark as Found) which don't touch photo
    @Query("""
        UPDATE lost_items
        SET status = :status,
            last_modified_at = :timestamp,
            sync_status = 'PENDING_SYNC'
        WHERE id = :id
    """)
    abstract suspend fun updateItemStatus(id: String, status: String, timestamp: Long)

    @Query("UPDATE lost_items SET photo_uri = :photoUri WHERE id = :id")
    abstract suspend fun updatePhotoUri(id: String, photoUri: String?)

    @Query("UPDATE lost_items SET sync_status = :syncStatus WHERE id = :id")
    abstract suspend fun updateSyncStatus(id: String, syncStatus: String)

    // ── Delete ───────────────────────────────────────────────────────────

    @Query("DELETE FROM lost_items WHERE id = :id")
    abstract suspend fun deleteItem(id: String)

    // ── Remote upsert (pull from Supabase) ───────────────────────────────
    //
    // PENDING_SYNC rows are never overwritten by a remote pull — the local
    // state is authoritative until it has been successfully pushed.

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertIfAbsent(item: LostItemEntity)

    @Query("""
        UPDATE lost_items SET
            title            = :title,
            description      = :description,
            location         = :location,
            status           = :status,
            last_modified_at = :lastModifiedAt,
            photo_uri        = CASE
                                   WHEN sync_status = 'PENDING_SYNC' THEN photo_uri
                                   WHEN :remotePhotoUri IS NOT NULL   THEN :remotePhotoUri
                                   ELSE photo_uri
                               END
        WHERE id = :id
    """)
    protected abstract suspend fun updateFromRemote(
        id: String,
        title: String,
        description: String,
        location: String?,
        status: String,
        lastModifiedAt: Long,
        remotePhotoUri: String?
    )

    open suspend fun upsertFromRemote(
        id: String,
        title: String,
        description: String,
        location: String?,
        status: String,
        reportedBy: String,
        reportedAt: Long,
        lastModifiedAt: Long,
        remotePhotoUri: String?
    ) {
        insertIfAbsent(
            LostItemEntity(
                id             = id,
                title          = title,
                description    = description,
                location       = location,
                status         = status,
                reportedBy     = reportedBy,
                reportedAt     = reportedAt,
                lastModifiedAt = lastModifiedAt,
                photoUri       = remotePhotoUri,
                syncStatus     = "SYNCED"
            )
        )
        updateFromRemote(
            id             = id,
            title          = title,
            description    = description,
            location       = location,
            status         = status,
            lastModifiedAt = lastModifiedAt,
            remotePhotoUri = remotePhotoUri
        )
    }
}