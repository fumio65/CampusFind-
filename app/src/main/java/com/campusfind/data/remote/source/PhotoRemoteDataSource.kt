package com.campusfind.data.remote.source

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import java.io.File
import javax.inject.Inject

class PhotoRemoteDataSource @Inject constructor(
    private val supabase: SupabaseClient
) {
    companion object {
        private const val ITEM_PHOTOS_BUCKET = "item-photos"
        private const val CLAIM_PHOTOS_BUCKET = "claim-photos"
    }

    /**
     * Upload a local file to Supabase Storage and return its public URL.
     * Returns null if the file doesn't exist or upload fails.
     */
    suspend fun uploadItemPhoto(localPath: String, itemId: String): String? {
        return uploadPhoto(localPath, ITEM_PHOTOS_BUCKET, "items/$itemId.jpg")
    }

    suspend fun uploadClaimPhoto(localPath: String, claimId: String, index: Int): String? {
        return uploadPhoto(localPath, CLAIM_PHOTOS_BUCKET, "claims/$claimId-$index.jpg")
    }

    private suspend fun uploadPhoto(localPath: String, bucket: String, remotePath: String): String? {
        return try {
            val file = File(localPath)
            if (!file.exists()) return null
            val bytes = file.readBytes()
            // Try upload; if the file already exists, update it instead
            try {
                supabase.storage.from(bucket).upload(remotePath, bytes)
            } catch (e: Exception) {
                supabase.storage.from(bucket).update(remotePath, bytes)
            }
            supabase.storage.from(bucket).publicUrl(remotePath)
        } catch (e: Exception) {
            null
        }
    }

    /** Returns true if the URI is already a remote URL (already uploaded). */
    fun isRemoteUrl(uri: String?): Boolean = uri?.startsWith("https://") == true
}
