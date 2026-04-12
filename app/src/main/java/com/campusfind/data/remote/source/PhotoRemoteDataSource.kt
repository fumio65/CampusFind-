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
        private const val ITEM_PHOTOS_BUCKET    = "item-photos"
        private const val CLAIM_PHOTOS_BUCKET   = "claim-photos"
        private const val PROFILE_PHOTOS_BUCKET = "profile-photos"
    }

    // ── Item photos ────────────────────────────────────────────────────────

    suspend fun uploadItemPhoto(localPath: String, itemId: String): String? {
        val remotePath = "items/$itemId/${UUID.randomUUID()}.jpg"
        return uploadPhoto(localPath, ITEM_PHOTOS_BUCKET, remotePath)
    }

    suspend fun deleteOldItemPhotos(itemId: String, keepUrl: String) {
        try {
            val files = supabase.storage.from(ITEM_PHOTOS_BUCKET).list("items/$itemId")
            val toDelete = files
                .map { "items/$itemId/${it.name}" }
                .filter { !keepUrl.endsWith(it) }
            if (toDelete.isNotEmpty()) supabase.storage.from(ITEM_PHOTOS_BUCKET).delete(toDelete)
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ── Claim photos ───────────────────────────────────────────────────────

    suspend fun uploadClaimPhoto(localPath: String, claimId: String, index: Int): String? {
        val remotePath = "claims/$claimId/${UUID.randomUUID()}-$index.jpg"
        return uploadPhoto(localPath, CLAIM_PHOTOS_BUCKET, remotePath)
    }

    // ── Profile photos ─────────────────────────────────────────────────────

    /**
     * Upload a profile photo for [userId].
     * Uses a unique UUID path so the CDN and Coil always serve fresh bytes
     * when the user changes their photo, instead of returning a cached version.
     * The old photo is deleted automatically after a successful upload.
     */
    suspend fun uploadProfilePhoto(localPath: String, userId: String): String? {
        val remotePath = "profiles/$userId/${UUID.randomUUID()}.jpg"
        val url = uploadPhoto(localPath, PROFILE_PHOTOS_BUCKET, remotePath) ?: return null
        // Delete previous photos for this user (keep only the new one)
        deleteOldProfilePhotos(userId, keepUrl = url)
        return url
    }

    private suspend fun deleteOldProfilePhotos(userId: String, keepUrl: String) {
        try {
            val files = supabase.storage.from(PROFILE_PHOTOS_BUCKET).list("profiles/$userId")
            val toDelete = files
                .map { "profiles/$userId/${it.name}" }
                .filter { !keepUrl.endsWith(it) }
            if (toDelete.isNotEmpty()) supabase.storage.from(PROFILE_PHOTOS_BUCKET).delete(toDelete)
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ── Shared upload helper ───────────────────────────────────────────────

    private suspend fun uploadPhoto(localPath: String, bucket: String, remotePath: String): String? {
        return try {
            val file = File(localPath)
            if (!file.exists()) return null
            supabase.storage.from(bucket).upload(remotePath, file.readBytes(), upsert = false)
            supabase.storage.from(bucket).publicUrl(remotePath)
        } catch (e: Exception) {
            null
        }
    }

    fun isRemoteUrl(uri: String?): Boolean = uri?.startsWith("https://") == true
}