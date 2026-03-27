package com.campusfind.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TipDao {

    @Query("""
        SELECT tips.*, users.full_name as author_name
        FROM tips
        INNER JOIN users ON tips.author_id = users.id
        WHERE tips.item_id = :itemId
        ORDER BY tips.created_at ASC
    """)
    fun getTipsByItemId(itemId: String): Flow<List<TipWithAuthor>>

    @Query("SELECT COUNT(*) FROM tips WHERE item_id = :itemId AND parent_tip_id IS NULL")
    suspend fun getTipCount(itemId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTip(tip: TipEntity)

    @Delete
    suspend fun deleteTip(tip: TipEntity)

    @Query("DELETE FROM tips WHERE item_id = :itemId")
    suspend fun deleteTipsByItem(itemId: String)
}

data class TipWithAuthor(
    @Embedded val tip: TipEntity,
    @ColumnInfo(name = "author_name") val authorName: String
)