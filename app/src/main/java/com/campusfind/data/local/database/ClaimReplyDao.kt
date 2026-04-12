package com.campusfind.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClaimReplyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReply(reply: ClaimReplyEntity)

    @Query("""
        SELECT 
            cr.id,
            cr.claim_id,
            cr.author_id,
            cr.message,
            cr.created_at,
            u.full_name as author_name
        FROM claim_replies cr
        INNER JOIN users u ON cr.author_id = u.id
        WHERE cr.claim_id = :claimId
        ORDER BY cr.created_at ASC
    """)
    fun getRepliesByClaimId(claimId: String): Flow<List<ClaimReplyWithUserEntity>>

    @Query("SELECT COUNT(*) FROM claim_replies WHERE claim_id = :claimId")
    suspend fun getReplyCountByClaimId(claimId: String): Int

    @Query("DELETE FROM claim_replies WHERE claim_id = :claimId")
    suspend fun deleteRepliesByClaimId(claimId: String)

    @Query("SELECT * FROM claim_replies WHERE sync_status = 'PENDING_SYNC'")
    suspend fun getPendingSyncReplies(): List<ClaimReplyEntity>

    @Query("UPDATE claim_replies SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(replies: List<ClaimReplyEntity>)
}

data class ClaimReplyWithUserEntity(
    val id: String,
    @ColumnInfo(name = "claim_id") val claimId: String,
    @ColumnInfo(name = "author_id") val authorId: String,
    val message: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "author_name") val authorName: String
)