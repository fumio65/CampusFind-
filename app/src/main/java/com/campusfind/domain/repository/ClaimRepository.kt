package com.campusfind.domain.repository

import com.campusfind.domain.model.Claim
import com.campusfind.domain.model.ClaimReply
import com.campusfind.domain.model.ClaimStatus
import kotlinx.coroutines.flow.Flow

interface ClaimRepository {

    suspend fun submitClaim(
        itemId: String,
        finderId: String,
        message: String,
        photoUrl: String?
    ): Result<Unit>

    fun getClaimsByItem(itemId: String): Flow<List<Claim>>

    suspend fun updateClaimStatus(claimId: String, status: ClaimStatus): Result<Unit>

    suspend fun deleteClaim(claimId: String): Result<Unit>

    suspend fun getClaimCountByItemId(itemId: String): Int

    suspend fun submitClaimReply(
        claimId: String,
        authorId: String,
        message: String
    ): Result<Unit>

    fun getRepliesByClaimId(claimId: String): Flow<List<ClaimReply>>

    suspend fun getReplyCountByClaimId(claimId: String): Int
}