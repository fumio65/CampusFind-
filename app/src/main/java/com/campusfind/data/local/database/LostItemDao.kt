package com.campusfind.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * FILE: app/src/main/java/com/campusfind/data/local/database/LostItemDao.kt
 *
 * Data Access Object for the `lost_items` table.
 *
 * Why some methods return Flow and others are suspend:
 * - Flow  → used for lists that need to auto-update the UI in real time (DEC-014)
 *           When Room data changes, Flow emits a new list automatically —
 *           no manual refresh needed in HomeScreen or DetailScreen
 * - suspend → used for one-shot write operations (insert, update, delete)
 *             and single-item reads that do not need to be observed over time
 *
 * Why ORDER BY reported_at DESC:
 * - Most recently reported items appear at the top of the list
 * - This matches the index on reported_at (DEC-015) for fast sorting
 *
 * Why updateItemStatus uses a raw @Query instead of @Update:
 * - @Update requires the full entity object
 * - We only need to change status and last_modified_at — no need to fetch
 *   the whole entity first just to update two fields
 * - Also sets sync_status = 'PENDING_SYNC' placeholder for Phase 2
 *
 * Why deleteItem uses item id directly via @Query instead of @Delete:
 * - @Delete requires the full entity object to be passed in
 * - We only have the id available in DetailViewModel — avoids an extra DB fetch
 *
 * See: DEC-003 (offline-first), DEC-006 (Room), DEC-014 (lazy loading),
 *      DEC-015 (indexes), DEC-020 (multi-user shared DB), TASK-103
 */
@Dao
interface LostItemDao {

    // ── READ — Flow (reactive, auto-updates UI) ───────────────────────────────

    @Query("SELECT * FROM lost_items ORDER BY reported_at DESC")
    fun getAllItems(): Flow<List<LostItemEntity>>
    // Used by HomeViewModel when filter = null (All tab)
    // Returns ALL items from ALL users — supports demo step 9 (User B sees User A's items)

    @Query("SELECT * FROM lost_items WHERE status = :status ORDER BY reported_at DESC")
    fun getItemsByStatus(status: String): Flow<List<LostItemEntity>>
    // Used by HomeViewModel when filter chip = LOST or FOUND
    // :status receives "LOST" or "FOUND" — stored as String per DEC-010

    @Query("SELECT * FROM lost_items WHERE reported_by = :userId ORDER BY reported_at DESC")
    fun getItemsByUser(userId: String): Flow<List<LostItemEntity>>
    // Used by UserProfileScreen to show only the current user's own reports

    // ── READ — suspend (one-shot, not observed) ───────────────────────────────

    @Query("SELECT * FROM lost_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: String): LostItemEntity?
    // Used by DetailViewModel to load a single item when the user taps an ItemCard
    // Returns null if item was deleted between navigation and screen load

    // ── WRITE ─────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LostItemEntity)
    // Used by LostItemRepositoryImpl.addItem()
    // REPLACE strategy handles edge case of re-inserting same UUID (should not happen
    // in practice since UUIDs are random, but prevents a crash if it does)

    @Query("""
        UPDATE lost_items 
        SET status          = :status,
            last_modified_at = :timestamp
        WHERE id = :id
    """)
    suspend fun updateItemStatus(id: String, status: String, timestamp: Long)
    // Used by LostItemRepositoryImpl.updateStatus()
    // Called when reporter taps "Mark as Found" (demo step 14)
    // timestamp = System.currentTimeMillis() passed in from repository layer
    // Note: sync_status column intentionally omitted — added in Phase 2 (TASK-206)

    @Query("DELETE FROM lost_items WHERE id = :id")
    suspend fun deleteItem(id: String)
    // Used by LostItemRepositoryImpl.deleteItem()
    // Called when reporter confirms delete from overflow menu (TASK-116)
    // Only the item id is needed — avoids fetching the full entity just to delete it

    @Query("""
        UPDATE lost_items 
        SET title = :title, 
            description = :description,
            last_modified_at = :timestamp
        WHERE id = :id
    """)
    suspend fun updateItemDetails(
        id: String,
        title: String,
        description: String,
        timestamp: Long
    )
}