package com.campusfind.data.remote.source

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import java.io.File
import java.util.UUID
import javax.inject.Inject

class PhotoRemoteDataSource @Inject constructor(
    private val supabase: SupabaseClient
) {
    companion object {
        private const val ITEM_PHOTOS_BUCKET  = "item-photos"
        private const val CLAIM_PHOTOS_BUCKET = "claim-photos"
    }

    /**
     * Upload a local file to Supabase Storage and return its public URL.
     * Each upload uses a unique path so the CDN and Coil always fetch fresh bytes.
     */
    suspend fun uploadItemPhoto(localPath: String, itemId: String): String? {
        val uniqueName = UUID.randomUUID().toString()
        val remotePath = "items/$itemId/$uniqueName.jpg"
        return uploadPhoto(localPath, ITEM_PHOTOS_BUCKET, remotePath)
    }

    suspend fun uploadClaimPhoto(localPath: String, claimId: String, index: Int): String? {
        val uniqueName = UUID.randomUUID().toString()
        val remotePath = "claims/$claimId/$uniqueName-$index.jpg"
        return uploadPhoto(localPath, CLAIM_PHOTOS_BUCKET, remotePath)
    }

    /**
     * Delete every uploaded photo for this item EXCEPT the one at [keepUrl].
     * Call this after a successful upload to remove stale files from Storage.
     * Non-fatal — if it fails the app still works, just with leftover files.
     */
    suspend fun deleteOldItemPhotos(itemId: String, keepUrl: String) {
        try {
            val files = supabase.storage
                .from(ITEM_PHOTOS_BUCKET)
                .list("items/$itemId")

            // Build the full remote path for each file in this item's folder
            val toDelete = files
                .map { "items/$itemId/${it.name}" }
                .filter { path ->
                    // Keep the file whose public URL ends with this path segment
                    !keepUrl.endsWith(path)
                }

            if (toDelete.isNotEmpty()) {
                supabase.storage.from(ITEM_PHOTOS_BUCKET).delete(toDelete)
            }
        } catch (e: Exception) {
            // Non-fatal — leftover files in Storage are acceptable
            e.printStackTrace()
        }
    }

    private suspend fun uploadPhoto(
        localPath: String,
        bucket: String,
        remotePath: String
    ): String? {
        return try {
            val file = File(localPath)
            if (!file.exists()) return null
            val bytes = file.readBytes()
            supabase.storage.from(bucket).upload(remotePath, bytes, upsert = false)
            supabase.storage.from(bucket).publicUrl(remotePath)
        } catch (e: Exception) {
            null
        }
    }

    fun isRemoteUrl(uri: String?): Boolean = uri?.startsWith("https://") == true
}