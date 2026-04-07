package com.campusfind.data.remote

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FirebaseStorageManager.kt
 *
 * Handles uploading photos to Firebase Storage and returning
 * a public download URL that works on any device.
 *
 * Storage structure:
 * - Item photos:  /item_photos/{userId}/{itemId}.jpg
 * - Claim photos: /claim_photos/{claimId}/{index}.jpg
 *
 * Why download URL instead of local path?
 * - Local path: /data/user/0/com.campusfind/files/item_123.jpg
 *   → only exists on the device that uploaded it ❌
 * - Download URL: https://firebasestorage.googleapis.com/...
 *   → accessible from any device, any platform ✅
 */
@Singleton
class FirebaseStorageManager @Inject constructor(
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) {
    /**
     * Upload a photo from a content URI (photo picker result)
     * Returns the Firebase Storage download URL, or null if upload fails
     */
    suspend fun uploadItemPhoto(
        contentUri: Uri,
        userId: String,
        itemId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val ref = storage.reference
                .child("item_photos")
                .child(userId)
                .child("$itemId.jpg")

            // Upload the file
            ref.putFile(contentUri).await()

            // Get and return the download URL
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Upload a photo from a local file path (already saved to internal storage)
     * Returns the Firebase Storage download URL, or null if upload fails
     */
    suspend fun uploadItemPhotoFromPath(
        localPath: String,
        userId: String,
        itemId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(localPath)
            if (!file.exists()) return@withContext null

            val ref = storage.reference
                .child("item_photos")
                .child(userId)
                .child("$itemId.jpg")

            ref.putFile(Uri.fromFile(file)).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Upload a claim proof photo
     * Returns download URL or null
     */
    suspend fun uploadClaimPhoto(
        contentUri: Uri,
        claimId: String,
        index: Int = 0
    ): String? = withContext(Dispatchers.IO) {
        try {
            val ref = storage.reference
                .child("claim_photos")
                .child(claimId)
                .child("photo_$index.jpg")

            ref.putFile(contentUri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Delete a photo from Firebase Storage by its download URL
     */
    suspend fun deletePhoto(downloadUrl: String) = withContext(Dispatchers.IO) {
        try {
            storage.getReferenceFromUrl(downloadUrl).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}