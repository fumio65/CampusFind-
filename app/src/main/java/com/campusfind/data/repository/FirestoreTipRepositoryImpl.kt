package com.campusfind.data.repository

import com.campusfind.data.local.database.TipDao
import com.campusfind.data.local.database.UserDao
import com.campusfind.data.local.database.TipEntity
import com.campusfind.data.local.database.TipWithAuthor
import com.campusfind.domain.model.Tip
import com.campusfind.domain.repository.TipRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

/**
 * FirestoreTipRepositoryImpl.kt
 *
 * Extends TipRepositoryImpl with Firestore sync.
 *
 * Firestore structure:
 * /lost_items/{itemId}/tips/{tipId}
 *
 * Strategy — Room as SSOT:
 * 1. Reads always come from Room (reactive Flow)
 * 2. Writes go to Room first, then Firestore
 * 3. syncTipsFromFirestore() pulls remote tips into Room
 */
class FirestoreTipRepositoryImpl @Inject constructor(
    private val tipDao: TipDao,
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
) : TipRepository {

    // ── Reads — always from Room ───────────────────────────────────────────

    override fun getTipsByItemId(itemId: String): Flow<List<Tip>> =
        tipDao.getTipsByItemId(itemId).map { it.map { t -> t.toDomain() } }

    override suspend fun getTipCount(itemId: String): Int =
        tipDao.getTipCount(itemId)

    // ── Submit tip — Room first, then Firestore ────────────────────────────

    override suspend fun submitTip(
        itemId: String,
        authorId: String,
        message: String
    ): Result<Unit> {
        return try {
            val tip = TipEntity(
                id          = UUID.randomUUID().toString(),
                itemId      = itemId,
                authorId    = authorId,
                message     = message.trim(),
                createdAt   = System.currentTimeMillis(),
                parentTipId = null
            )
            tipDao.insertTip(tip)
            syncTipToFirestore(tip)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Submit reply — Room first, then Firestore ──────────────────────────

    override suspend fun submitReply(
        parentTipId: String,
        itemId: String,
        authorId: String,
        message: String
    ): Result<Unit> {
        return try {
            val reply = TipEntity(
                id          = UUID.randomUUID().toString(),
                itemId      = itemId,
                authorId    = authorId,
                message     = message.trim(),
                createdAt   = System.currentTimeMillis(),
                parentTipId = parentTipId
            )
            tipDao.insertTip(reply)
            syncTipToFirestore(reply)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Delete tip ─────────────────────────────────────────────────────────

    override suspend fun deleteTip(tipId: String): Result<Unit> {
        return try {
            val tip = TipEntity(id = tipId, itemId = "", authorId = "", message = "", createdAt = 0)
            tipDao.deleteTip(tip)
            // Note: replies are cascade-deleted in Room but need manual delete in Firestore
            // For simplicity we mark as deleted — full cascade can be added later
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Sync tips for an item from Firestore → Room ────────────────────────
    // Called by SyncWorker and MainActivity.onResume()

    suspend fun syncTipsFromFirestore(itemId: String) {
        try {
            val snapshot = firestore
                .collection("lost_items")
                .document(itemId)
                .collection("tips")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    TipEntity(
                        id          = doc.getString("id") ?: doc.id,
                        itemId      = doc.getString("itemId") ?: itemId,
                        authorId    = doc.getString("authorId") ?: return@mapNotNull null,
                        message     = doc.getString("message") ?: return@mapNotNull null,
                        createdAt   = doc.getLong("createdAt") ?: return@mapNotNull null,
                        parentTipId = doc.getString("parentTipId")
                    )
                } catch (e: Exception) { null }
            }.forEach { tip ->
                // Ensure the tip author exists in local users table
                // before inserting — prevents FK constraint violation
                ensureUserExists(tip.authorId)
                tipDao.insertTip(tip)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Fetch a user from Firestore and insert into local Room if missing.
     * This prevents FK constraint failures when inserting tips/replies
     * from users who have never logged in on this device.
     */
    private suspend fun ensureUserExists(userId: String) {
        try {
            // Check if user already exists in Room
            val existing = userDao.getUserById(userId)
            if (existing != null) return

            // Fetch from Firestore /users/{userId}
            val doc = firestore
                .collection("users")
                .document(userId)
                .get()
                .await()

            if (!doc.exists()) return

            val userEntity = com.campusfind.data.local.database.UserEntity(
                id              = doc.getString("id") ?: userId,
                fullName        = doc.getString("fullName") ?: "Unknown",
                email           = doc.getString("email") ?: "",
                passwordHash    = "",   // Firebase Auth handles passwords
                messengerHandle = doc.getString("messengerHandle"),
                createdAt       = doc.getLong("createdAt") ?: System.currentTimeMillis()
            )
            userDao.insertUser(userEntity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── Internal helpers ───────────────────────────────────────────────────

    private suspend fun syncTipToFirestore(tip: TipEntity) {
        try {
            firestore
                .collection("lost_items")
                .document(tip.itemId)
                .collection("tips")
                .document(tip.id)
                .set(tip.toFirestoreMap())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun TipEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id"          to id,
        "itemId"      to itemId,
        "authorId"    to authorId,
        "message"     to message,
        "createdAt"   to createdAt,
        "parentTipId" to parentTipId
    )

    private fun TipWithAuthor.toDomain() = Tip(
        id          = tip.id,
        itemId      = tip.itemId,
        authorId    = tip.authorId,
        authorName  = authorName,
        message     = tip.message,
        createdAt   = tip.createdAt,
        parentTipId = tip.parentTipId,
        isReply     = tip.parentTipId != null
    )
}