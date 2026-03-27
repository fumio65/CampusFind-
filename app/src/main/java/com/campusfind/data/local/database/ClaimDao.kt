package com.campusfind.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for claims table
 */
@Dao
interface ClaimDao {

    /**
     * Insert a new claim
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaim(claim: ClaimEntity)

    /**
     * Get all claims for a specific item with claimer names (JOIN with users table)
     */
    @Query("""
        SELECT 
            c.id,
            c.item_id,
            c.claimed_by,
            c.message,
            c.photo_uri,
            c.status,
            c.claimed_at,
            u.full_name as claimer_name,
            u.messenger_handle as messenger_username
        FROM claims c
        INNER JOIN users u ON c.claimed_by = u.id
        WHERE c.item_id = :itemId
        ORDER BY c.claimed_at DESC
    """)
    fun getClaimsByItemId(itemId: String): Flow<List<ClaimWithUserEntity>>

    /**
     * Get claim count for an item
     */
    @Query("SELECT COUNT(*) FROM claims WHERE item_id = :itemId")
    suspend fun getClaimCountByItemId(itemId: String): Int

    /**
     * Update claim status (approve/reject)
     */
    @Query("UPDATE claims SET status = :status WHERE id = :claimId")
    suspend fun updateClaimStatus(claimId: String, status: String)

    /**
     * Delete a claim
     */
    @Query("DELETE FROM claims WHERE id = :claimId")
    suspend fun deleteClaim(claimId: String)

    /**
     * Get a single claim by ID
     */
    @Query("SELECT * FROM claims WHERE id = :claimId")
    suspend fun getClaimById(claimId: String): ClaimEntity?
}

/**
 * Data class for claim with user info (JOIN result)
 */
data class ClaimWithUserEntity(
    val id: String,
    @ColumnInfo(name = "item_id") val itemId: String,
    @ColumnInfo(name = "claimed_by") val claimedBy: String,
    val message: String,
    @ColumnInfo(name = "photo_uri") val photoUri: String?,
    val status: String,
    @ColumnInfo(name = "claimed_at") val claimedAt: Long,
    @ColumnInfo(name = "claimer_name") val claimerName: String,
    @ColumnInfo(name = "messenger_username") val messengerUsername: String?  // ✅ NEW: For Messenger deep link
)