package com.campusfind.data.repository

import android.content.Context
import android.net.Uri
import com.campusfind.data.local.database.ClaimDao
import com.campusfind.data.local.database.ClaimEntity
import com.campusfind.data.local.database.ClaimReplyDao
import com.campusfind.data.local.database.ClaimReplyEntity
import com.campusfind.data.local.database.ClaimReplyWithUserEntity
import com.campusfind.data.local.database.ClaimWithUserEntity
import com.campusfind.domain.model.Claim
import com.campusfind.domain.model.ClaimReply
import com.campusfind.domain.model.ClaimStatus
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
    @ApplicationContext private val context: Context
) : ClaimRepository {

    override suspend fun submitClaim(
        itemId: String,
        finderId: String,
        message: String,
        photoUrl: String?
    ): Result<Unit> {
        return try {
            val savedPhotoPath = photoUrl?.let { uri ->
                savePhotoToInternalStorage(uri)
            }

            if (photoUrl != null && savedPhotoPath == null) {
                return Result.failure(Exception("Failed to save photo. Please try again."))
            }

            val claim = ClaimEntity(
                id = UUID.randomUUID().toString(),
                itemId = itemId,
                claimedBy = finderId,
                message = message,
                photoUri = savedPhotoPath,
                status = "PENDING",
                claimedAt = System.currentTimeMillis()
            )

            claimDao.insertClaim(claim)
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
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteClaim(claimId: String): Result<Unit> {
        return try {
            val claim = claimDao.getClaimById(claimId)
            claim?.photoUri?.let { photoPath ->
                deletePhotoFile(photoPath)
            }

            claimDao.deleteClaim(claimId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getClaimCountByItemId(itemId: String): Int {
        return claimDao.getClaimCountByItemId(itemId)
    }

    override suspend fun submitClaimReply(
        claimId: String,
        authorId: String,
        message: String
    ): Result<Unit> {
        return try {
            val reply = ClaimReplyEntity(
                id = UUID.randomUUID().toString(),
                claimId = claimId,
                authorId = authorId,
                message = message.trim(),
                createdAt = System.currentTimeMillis()
            )

            claimReplyDao.insertReply(reply)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getRepliesByClaimId(claimId: String): Flow<List<ClaimReply>> {
        return claimReplyDao.getRepliesByClaimId(claimId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getReplyCountByClaimId(claimId: String): Int {
        return claimReplyDao.getReplyCountByClaimId(claimId)
    }

    private suspend fun savePhotoToInternalStorage(
        contentUri: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(contentUri)
            val inputStream = context.contentResolver.openInputStream(uri)

            if (inputStream == null) {
                return@withContext null
            }

            val fileName = "claim_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)

            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun deletePhotoFile(filePath: String) = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun ClaimWithUserEntity.toDomainModel() = Claim(
        id = id,
        itemId = itemId,
        claimerId = claimedBy,
        claimerName = claimerName,
        message = message,
        photoUri = photoUri,
        status = ClaimStatus.valueOf(status),
        claimedAt = claimedAt,
        messengerUsername = messengerUsername
    )

    private fun ClaimReplyWithUserEntity.toDomainModel() = ClaimReply(
        id = id,
        claimId = claimId,
        authorId = authorId,
        authorName = authorName,
        message = message,
        createdAt = createdAt
    )
}