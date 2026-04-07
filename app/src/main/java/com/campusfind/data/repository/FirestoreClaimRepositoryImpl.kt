package com.campusfind.data.repository

import android.content.Context
import android.net.Uri
import com.campusfind.data.local.database.ClaimDao
import com.campusfind.data.local.database.UserDao
import com.campusfind.data.local.database.ClaimEntity
import com.campusfind.data.local.database.ClaimReplyDao
import com.campusfind.data.local.database.ClaimReplyEntity
import com.campusfind.data.local.database.ClaimReplyWithUserEntity
import com.campusfind.data.local.database.ClaimWithUserEntity
import com.campusfind.domain.model.Claim
import com.campusfind.domain.model.ClaimReply
import com.campusfind.domain.model.ClaimStatus
import com.campusfind.domain.repository.ClaimRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

/**
 * FirestoreClaimRepositoryImpl.kt
 *
 * Extends ClaimRepositoryImpl with Firestore sync.
 *
 * Firestore structure:
 * /lost_items/{itemId}/claims/{claimId}
 * /lost_items/{itemId}/claims/{claimId}/replies/{replyId}
 *
 * Strategy — Room as SSOT:
 * 1. Reads always come from Room (reactive Flow)
 * 2. Writes go to Room first, then Firestore
 * 3. syncClaimsFromFirestore() pulls remote claims into Room
 */
class FirestoreClaimRepositoryImpl @Inject constructor(
    private val claimDao: ClaimDao,
    private val claimReplyDao: ClaimReplyDao,
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : ClaimRepository {

    companion object {
        private const val PHOTO_DELIMITER = "|"
    }

    // ── Reads — always from Room ───────────────────────────────────────────

    override fun getClaimsByItem(itemId: String): Flow<List<Claim>> =
        claimDao.getClaimsByItemId(itemId).map { it.map { e -> e.toDomainModel() } }

    override fun getRepliesByClaimId(claimId: String): Flow<List<ClaimReply>> =
        claimReplyDao.getRepliesByClaimId(claimId).map { it.map { e -> e.toDomainModel() } }

    override suspend fun getReplyCountByClaimId(claimId: String): Int =
        claimReplyDao.getReplyCountByClaimId(claimId)

    override suspend fun getClaimCountByItemId(itemId: String): Int =
        claimDao.getClaimCountByItemId(itemId)

    // ── Submit claim — Room first, then Firestore ──────────────────────────

    override suspend fun submitClaim(
        itemId: String,
        finderId: String,
        message: String,
        photoUrl: String?
    ): Result<Unit> {
        return try {
            val photoList = photoUrl
                ?.split("|")
                ?.filter { it.isNotBlank() }
                ?: emptyList()

            val savedPaths = photoList.mapNotNull { savePhotoToInternalStorage(it) }
            val photoUriValue = savedPaths.joinToString(PHOTO_DELIMITER).ifBlank { null }

            val claim = ClaimEntity(
                id        = UUID.randomUUID().toString(),
                itemId    = itemId,
                claimedBy = finderId,
                message   = message,
                photoUri  = photoUriValue,
                status    = "PENDING",
                claimedAt = System.currentTimeMillis()
            )

            claimDao.insertClaim(claim)
            syncClaimToFirestore(claim)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Update claim status — Room + Firestore ─────────────────────────────

    override suspend fun updateClaimStatus(claimId: String, status: ClaimStatus): Result<Unit> {
        return try {
            claimDao.updateClaimStatus(claimId, status.name)

            // Find which item this claim belongs to for Firestore path
            val claim = claimDao.getClaimById(claimId)
            if (claim != null) {
                firestore
                    .collection("lost_items")
                    .document(claim.itemId)
                    .collection("claims")
                    .document(claimId)
                    .update("status", status.name)
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Delete claim ───────────────────────────────────────────────────────

    override suspend fun deleteClaim(claimId: String): Result<Unit> {
        return try {
            val claim = claimDao.getClaimById(claimId)
            claim?.photoUri?.split(PHOTO_DELIMITER)?.forEach { deletePhotoFile(it) }

            if (claim != null) {
                firestore
                    .collection("lost_items")
                    .document(claim.itemId)
                    .collection("claims")
                    .document(claimId)
                    .delete()
                    .await()
            }
            claimDao.deleteClaim(claimId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Submit claim reply — Room + Firestore ──────────────────────────────

    override suspend fun submitClaimReply(
        claimId: String,
        authorId: String,
        message: String
    ): Result<Unit> {
        return try {
            val reply = ClaimReplyEntity(
                id        = UUID.randomUUID().toString(),
                claimId   = claimId,
                authorId  = authorId,
                message   = message.trim(),
                createdAt = System.currentTimeMillis()
            )
            claimReplyDao.insertReply(reply)
            syncReplyToFirestore(reply)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Sync claims for an item from Firestore → Room ──────────────────────

    suspend fun syncClaimsFromFirestore(itemId: String) {
        try {
            val snapshot = firestore
                .collection("lost_items")
                .document(itemId)
                .collection("claims")
                .orderBy("claimedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                try {
                    val claim = ClaimEntity(
                        id        = doc.getString("id") ?: doc.id,
                        itemId    = doc.getString("itemId") ?: itemId,
                        claimedBy = doc.getString("claimedBy") ?: return@forEach,
                        message   = doc.getString("message") ?: return@forEach,
                        photoUri  = doc.getString("photoUri"),
                        status    = doc.getString("status") ?: "PENDING",
                        claimedAt = doc.getLong("claimedAt") ?: return@forEach
                    )
                    // Ensure claimer exists in local Room before inserting
                    ensureUserExists(claim.claimedBy)
                    claimDao.insertClaim(claim)

                    // Also sync replies for this claim
                    syncRepliesFromFirestore(itemId, claim.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun syncRepliesFromFirestore(itemId: String, claimId: String) {
        try {
            val snapshot = firestore
                .collection("lost_items")
                .document(itemId)
                .collection("claims")
                .document(claimId)
                .collection("replies")
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                try {
                    val reply = ClaimReplyEntity(
                        id        = doc.getString("id") ?: doc.id,
                        claimId   = doc.getString("claimId") ?: claimId,
                        authorId  = doc.getString("authorId") ?: return@forEach,
                        message   = doc.getString("message") ?: return@forEach,
                        createdAt = doc.getLong("createdAt") ?: return@forEach
                    )
                    claimReplyDao.insertReply(reply)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── Internal helpers ───────────────────────────────────────────────────

    private suspend fun syncClaimToFirestore(claim: ClaimEntity) {
        try {
            firestore
                .collection("lost_items")
                .document(claim.itemId)
                .collection("claims")
                .document(claim.id)
                .set(claim.toFirestoreMap())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun syncReplyToFirestore(reply: ClaimReplyEntity) {
        try {
            // Find itemId via claimId
            val claim = claimDao.getClaimById(reply.claimId) ?: return
            firestore
                .collection("lost_items")
                .document(claim.itemId)
                .collection("claims")
                .document(reply.claimId)
                .collection("replies")
                .document(reply.id)
                .set(reply.toFirestoreMap())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun ClaimEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id"        to id,
        "itemId"    to itemId,
        "claimedBy" to claimedBy,
        "message"   to message,
        "photoUri"  to photoUri,
        "status"    to status,
        "claimedAt" to claimedAt
    )

    private fun ClaimReplyEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id"        to id,
        "claimId"   to claimId,
        "authorId"  to authorId,
        "message"   to message,
        "createdAt" to createdAt
    )

    private suspend fun savePhotoToInternalStorage(contentUri: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(contentUri)
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
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

    private fun ClaimWithUserEntity.toDomainModel() = Claim(
        id                = id,
        itemId            = itemId,
        claimerId         = claimedBy,
        claimerName       = claimerName,
        message           = message,
        photoUri          = photoUri?.split("|")?.firstOrNull(),
        photoUris         = photoUri?.split("|")?.filter { it.isNotBlank() } ?: emptyList(),
        status            = ClaimStatus.valueOf(status),
        claimedAt         = claimedAt,
        messengerUsername = messengerUsername
    )

    private suspend fun ensureUserExists(userId: String) {
        try {
            if (userDao.getUserById(userId) != null) return
            val doc = firestore.collection("users").document(userId).get().await()
            if (!doc.exists()) return
            val userEntity = com.campusfind.data.local.database.UserEntity(
                id              = doc.getString("id") ?: userId,
                fullName        = doc.getString("fullName") ?: "Unknown",
                email           = doc.getString("email") ?: "",
                passwordHash    = "",
                messengerHandle = doc.getString("messengerHandle"),
                createdAt       = doc.getLong("createdAt") ?: System.currentTimeMillis()
            )
            userDao.insertUser(userEntity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun ClaimReplyWithUserEntity.toDomainModel() = ClaimReply(
        id         = id,
        claimId    = claimId,
        authorId   = authorId,
        authorName = authorName,
        message    = message,
        createdAt  = createdAt
    )
}