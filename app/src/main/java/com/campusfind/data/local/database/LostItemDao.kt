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

    @Query("""
        UPDATE lost_items
        SET photo_uri = :photoUri,
            last_modified_at = :timestamp
        WHERE id = :id
    """)
    suspend fun updatePhotoUri(id: String, photoUri: String, timestamp: Long)

    @Query("DELETE FROM lost_items WHERE id = :id")
    suspend fun deleteItem(id: String)
}