package com.campusfind.data.repository

import android.content.Context
import android.net.Uri
import com.campusfind.data.local.database.ClaimDao
import com.campusfind.data.local.database.ClaimEntity
import com.campusfind.data.local.database.ClaimReplyDao
import com.campusfind.data.local.database.ClaimReplyEntity
import com.campusfind.data.local.database.ClaimReplyWithUserEntity
import com.campusfind.data.local.database.ClaimWithUserEntity
import com.campusfind.data.sync.SyncManager
import com.campusfind.domain.model.Claim
import com.campusfind.domain.model.ClaimReply
import com.campusfind.domain.model.ClaimStatus
import com.campusfind.domain.model.SyncStatus
import com.campusfind.domain.repository.ClaimRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

class ClaimRepositoryImpl @Inject constructor(
    private val claimDao: ClaimDao,
    private val claimReplyDao: ClaimReplyDao,
    @ApplicationContext private val context: Context,
    private val syncManager: SyncManager
) : ClaimRepository {

    companion object {
        // Delimiter for storing multiple photo paths in a single column
        private const val PHOTO_DELIMITER = "|"
    }

    // ── submitClaim — accepts multiple photos ──────────────────────────────

    override suspend fun submitClaim(
        itemId: String,
        finderId: String,
        message: String,
        photoUrl: String?          // may be pipe-delimited e.g. "uri1|uri2|uri3"
    ): Result<Unit> {
        // Split pipe-delimited URIs if multiple were passed
        val photoList = photoUrl
            ?.split("|")
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        return submitClaimWithPhotos(itemId, finderId, message, photoList)
    }

    // New overload used by IFoundThisItemSection — supports up to 3 photos
    suspend fun submitClaimWithPhotos(
        itemId: String,
        finderId: String,
        message: String,
        photoUrls: List<String>    // list of content URIs from photo picker
    ): Result<Unit> {
        return try {
            // Save each photo to internal storage
            val savedPaths = photoUrls.mapNotNull { uri ->
                savePhotoToInternalStorage(uri)
            }

            // Require at least one saved photo if any were provided
            if (photoUrls.isNotEmpty() && savedPaths.isEmpty()) {
                return Result.failure(Exception("Failed to save photos. Please try again."))
            }

            // Store paths as pipe-delimited string — no schema change needed
            val photoUriValue = if (savedPaths.isNotEmpty())
                savedPaths.joinToString(PHOTO_DELIMITER)
            else null

            val claim = ClaimEntity(
                id        = UUID.randomUUID().toString(),
                itemId    = itemId,
                claimedBy = finderId,
                message   = message,
                photoUri  = photoUriValue,
                status    = "PENDING",
                claimedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING_SYNC.name
            )

            claimDao.insertClaim(claim)
            syncManager.triggerNow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getClaimsByItem(itemId: String): Flow<List<Claim>> {
        return claimDao.getClaimsByItemId(itemId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun updateClaimStatus(claimId: String, status: ClaimStatus): Result<Unit> {
        return try {
            claimDao.updateClaimStatus(claimId, status.name)
            claimDao.updateSyncStatus(claimId, SyncStatus.PENDING_SYNC.name)
            syncManager.triggerNow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteClaim(claimId: String): Result<Unit> {
        return try {
            val claim = claimDao.getClaimById(claimId)
            // Delete all stored photo files
            claim?.photoUri?.split(PHOTO_DELIMITER)?.forEach { path ->
                deletePhotoFile(path)
            }
            claimDao.deleteClaim(claimId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getClaimCountByItemId(itemId: String): Int =
        claimDao.getClaimCountByItemId(itemId)

    override suspend fun submitClaimReply(
        claimId: String,
        authorId: String,
        message: String
    ): Result<Unit> {
        return try {
            val reply = ClaimReplyEntity(
                id       = UUID.randomUUID().toString(),
                claimId  = claimId,
                authorId = authorId,
                message  = message.trim(),
                createdAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING_SYNC.name
            )
            claimReplyDao.insertReply(reply)
            syncManager.triggerNow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getRepliesByClaimId(claimId: String): Flow<List<ClaimReply>> =
        claimReplyDao.getRepliesByClaimId(claimId).map { it.map { e -> e.toDomainModel() } }

    override suspend fun getReplyCountByClaimId(claimId: String): Int =
        claimReplyDao.getReplyCountByClaimId(claimId)

    // ── Internal helpers ───────────────────────────────────────────────────

    private suspend fun savePhotoToInternalStorage(contentUri: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(contentUri)
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext null

                val fileName = "claim_${UUID.randomUUID()}.jpg"
                val file = File(context.filesDir, fileName)
                file.outputStream().use { inputStream.copyTo(it) }
                inputStream.close()
                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    private suspend fun deletePhotoFile(filePath: String) = withContext(Dispatchers.IO) {
        try { File(filePath).takeIf { it.exists() }?.delete() }
        catch (e: Exception) { e.printStackTrace() }
    }

    // ── Mapping ────────────────────────────────────────────────────────────

    private fun ClaimWithUserEntity.toDomainModel() = Claim(
        id               = id,
        itemId           = itemId,
        claimerId        = claimedBy,
        claimerName      = claimerName,
        message          = message,
        // Split pipe-delimited paths back into a list, expose first as photoUri
        // for backward-compat; full list available via photoUris
        photoUri         = photoUri?.split("|")?.firstOrNull(),
        photoUris        = photoUri?.split("|")?.filter { it.isNotBlank() } ?: emptyList(),
        status           = ClaimStatus.valueOf(status),
        claimedAt        = claimedAt,
        messengerUsername = messengerUsername
    )

    private fun ClaimReplyWithUserEntity.toDomainModel() = ClaimReply(
        id         = id,
        claimId    = claimId,
        authorId   = authorId,
        authorName = authorName,
        message    = message,
        createdAt  = createdAt
    )
}