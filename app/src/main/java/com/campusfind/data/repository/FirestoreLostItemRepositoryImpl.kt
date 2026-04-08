package com.campusfind.data.repository

import com.campusfind.data.local.database.LostItemDao
import com.campusfind.data.local.database.LostItemEntity
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import com.campusfind.domain.repository.LostItemRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class FirestoreLostItemRepositoryImpl @Inject constructor(
    private val dao: LostItemDao,
    private val sessionManager: SessionManager,
    private val firestore: FirebaseFirestore
) : LostItemRepository {

    companion object {
        private const val ITEMS_COLLECTION = "lost_items"
    }

    // ── Reads — always from Room ───────────────────────────────────────────

    override fun getAllItems(): Flow<List<LostItem>> =
        dao.getAllItems().map { it.map { e -> e.toDomain() } }

    override fun getItemsByStatus(status: ItemStatus): Flow<List<LostItem>> =
        dao.getItemsByStatus(status.name).map { it.map { e -> e.toDomain() } }

    override suspend fun getItemById(id: String): LostItem? =
        dao.getItemById(id)?.toDomain()

    override fun getItemsByUser(userId: String): Flow<List<LostItem>> =
        dao.getItemsByUser(userId).map { it.map { e -> e.toDomain() } }

    // ── Add item — Room first (local path), then Firestore ─────────────────
    // Returns item ID so caller can update photoUri after Supabase upload

    override suspend fun addItem(
        title: String,
        description: String,
        location: String?,
        photoUri: String?
    ): Result<String> {
        return try {
            val userId = sessionManager.currentUserId
                ?: return Result.failure(Exception("Not logged in"))

            val itemId = UUID.randomUUID().toString()
            val now    = System.currentTimeMillis()

            val entity = LostItemEntity(
                id             = itemId,
                title          = title,
                description    = description,
                location       = location,
                status         = "LOST",
                reportedBy     = userId,
                reportedAt     = now,
                lastModifiedAt = now,
                photoUri       = photoUri   // local path at this point
            )

            // 1. Save to Room immediately — photo shows on reporter's device instantly
            dao.insertItem(entity)

            // 2. Sync to Firestore (with local path for now)
            syncItemToFirestore(entity)

            Result.success(itemId)   // ← return itemId
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Update photo URI — called after Supabase upload completes ──────────
    // Updates both Room and Firestore with the public HTTPS URL

    override suspend fun updatePhotoUri(id: String, photoUri: String): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()

            // Update Room
            dao.updatePhotoUri(id, photoUri, timestamp)

            // Update Firestore
            firestore.collection(ITEMS_COLLECTION).document(id)
                .update(
                    mapOf(
                        "photoUri"       to photoUri,
                        "lastModifiedAt" to timestamp
                    )
                ).await()

            android.util.Log.d("Supabase", "✅ photoUri updated in Room + Firestore: $photoUri")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ── Update item details ────────────────────────────────────────────────

    override suspend fun updateItemDetails(
        id: String,
        title: String,
        description: String,
        location: String?
    ): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            dao.updateItemDetailsWithLocation(id, title, description, location, timestamp)

            firestore.collection(ITEMS_COLLECTION).document(id)
                .update(mapOf(
                    "title"          to title,
                    "description"    to description,
                    "location"       to location,
                    "lastModifiedAt" to timestamp
                )).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Update item status ─────────────────────────────────────────────────

    override suspend fun updateItemStatus(id: String, status: ItemStatus): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            dao.updateItemStatus(id, status.name, timestamp)

            firestore.collection(ITEMS_COLLECTION).document(id)
                .update(mapOf(
                    "status"         to status.name,
                    "lastModifiedAt" to timestamp
                )).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Delete item ────────────────────────────────────────────────────────

    override suspend fun deleteItem(id: String): Result<Unit> {
        return try {
            dao.deleteItem(id)
            firestore.collection(ITEMS_COLLECTION).document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Sync from Firestore to Room ────────────────────────────────────────

    suspend fun syncFromFirestore() {
        try {
            val snapshot = firestore.collection(ITEMS_COLLECTION)
                .orderBy("reportedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val entities = snapshot.documents.mapNotNull { doc -> doc.toEntity() }
            entities.forEach { dao.insertItem(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── Internal helpers ───────────────────────────────────────────────────

    private suspend fun syncItemToFirestore(entity: LostItemEntity) {
        try {
            firestore.collection(ITEMS_COLLECTION)
                .document(entity.id)
                .set(entity.toFirestoreMap())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun LostItemEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id"             to id,
        "title"          to title,
        "description"    to description,
        "location"       to location,
        "status"         to status,
        "reportedBy"     to reportedBy,
        "reportedAt"     to reportedAt,
        "lastModifiedAt" to lastModifiedAt,
        "photoUri"       to photoUri
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toEntity(): LostItemEntity? {
        return try {
            LostItemEntity(
                id             = getString("id") ?: id,
                title          = getString("title") ?: return null,
                description    = getString("description") ?: return null,
                location       = getString("location"),
                status         = getString("status") ?: "LOST",
                reportedBy     = getString("reportedBy") ?: return null,
                reportedAt     = getLong("reportedAt") ?: return null,
                lastModifiedAt = getLong("lastModifiedAt") ?: System.currentTimeMillis(),
                photoUri       = getString("photoUri")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun LostItemEntity.toDomain() = LostItem(
        id             = id,
        title          = title,
        description    = description,
        location       = location,
        status         = ItemStatus.valueOf(status),
        reportedBy     = reportedBy,
        reportedAt     = reportedAt,
        lastModifiedAt = lastModifiedAt,
        photoUri       = photoUri
    )
}