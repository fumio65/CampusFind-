package com.campusfind.data.remote.source

import android.util.Log
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
        private const val TAG = "SYNC_DEBUG"
    }

    // ── Item photos ────────────────────────────────────────────────────────

    suspend fun uploadItemPhoto(localPath: String, itemId: String): String? {
        val remotePath = "items/$itemId/${UUID.randomUUID()}.jpg"
        return uploadPhoto(localPath, ITEM_PHOTOS_BUCKET, remotePath)
    }

    suspend fun deleteOldItemPhotos(itemId: String, keepUrl: String) {
        try {
            val files = supabase.storage[ITEM_PHOTOS_BUCKET].list("items/$itemId")
            val toDelete = files
                .map { "items/$itemId/${it.name}" }
                .filter { !keepUrl.endsWith(it) }
            if (toDelete.isNotEmpty()) supabase.storage[ITEM_PHOTOS_BUCKET].delete(toDelete)
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ── Claim photos ───────────────────────────────────────────────────────

    suspend fun uploadClaimPhoto(localPath: String, claimId: String, index: Int): String? {
        val remotePath = "claims/$claimId/${UUID.randomUUID()}-$index.jpg"
        return uploadPhoto(localPath, CLAIM_PHOTOS_BUCKET, remotePath)
    }

    // ── Profile photos ─────────────────────────────────────────────────────

    suspend fun uploadProfilePhoto(localPath: String, userId: String): String? {
        return try {
            val file = File(localPath)
            Log.d(TAG, "uploadProfilePhoto: path=$localPath exists=${file.exists()} size=${file.length()}")
            if (!file.exists()) {
                Log.e(TAG, "uploadProfilePhoto: file does not exist!")
                return null
            }
            val remotePath = "profiles/$userId/${UUID.randomUUID()}.jpg"
            val bytes = file.readBytes()
            Log.d(TAG, "uploadProfilePhoto: uploading ${bytes.size} bytes to bucket=$PROFILE_PHOTOS_BUCKET path=$remotePath")
            supabase.storage[PROFILE_PHOTOS_BUCKET].upload(remotePath, bytes, upsert = true)
            val url = supabase.storage[PROFILE_PHOTOS_BUCKET].publicUrl(remotePath)
            Log.d(TAG, "uploadProfilePhoto: SUCCESS url=$url")
            deleteOldProfilePhotos(userId, keepUrl = url)
            url
        } catch (e: Exception) {
            Log.e(TAG, "uploadProfilePhoto EXCEPTION: ${e::class.simpleName}: ${e.message}", e)
            null
        }
    }

    private suspend fun deleteOldProfilePhotos(userId: String, keepUrl: String) {
        try {
            val files = supabase.storage[PROFILE_PHOTOS_BUCKET].list("profiles/$userId")
            val toDelete = files
                .map { "profiles/$userId/${it.name}" }
                .filter { !keepUrl.endsWith(it) }
            if (toDelete.isNotEmpty()) supabase.storage[PROFILE_PHOTOS_BUCKET].delete(toDelete)
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ── Shared upload helper ───────────────────────────────────────────────

    private suspend fun uploadPhoto(localPath: String, bucket: String, remotePath: String): String? {
        return try {
            val file = File(localPath)
            if (!file.exists()) return null
            supabase.storage[bucket].upload(remotePath, file.readBytes(), upsert = false)
            supabase.storage[bucket].publicUrl(remotePath)
        } catch (e: Exception) {
            Log.e(TAG, "uploadPhoto FAILED bucket=$bucket path=$remotePath: ${e.message}")
            null
        }
    }

    fun isRemoteUrl(uri: String?): Boolean = uri?.startsWith("https://") == true
}