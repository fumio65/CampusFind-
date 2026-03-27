package com.campusfind.data.local.photo

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PhotoManager - Handles offline-first photo storage
 *
 * STRATEGY:
 * 1. User selects photo → we COPY it to app's internal storage
 * 2. Store the INTERNAL file path in Room (not content:// URI)
 * 3. Photos persist even if original is deleted from gallery
 * 4. Photos are private to the app
 *
 * WHY THIS FIXES THE ISSUE:
 * - content:// URIs can become invalid (file moved/deleted)
 * - Internal storage is guaranteed to exist
 * - Coil loads local files instantly (no permission issues)
 */
@Singleton
class PhotoManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val photosDir: File by lazy {
        File(context.filesDir, "item_photos").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Save a photo from URI to internal storage
     *
     * @param uri The content:// URI from photo picker
     * @return Internal file path (e.g., "/data/user/0/.../files/item_photos/abc123.jpg")
     *         This path is what you store in Room's photo_uri column
     */
    fun savePhoto(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return null

            // Generate unique filename
            val filename = "${UUID.randomUUID()}.jpg"
            val outputFile = File(photosDir, filename)

            // Copy to internal storage
            FileOutputStream(outputFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            // Return the INTERNAL file path (not content:// URI)
            outputFile.absolutePath

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Delete a photo from internal storage
     * Call this when user deletes an item
     */
    fun deletePhoto(filePath: String?) {
        if (filePath == null) return
        try {
            val file = File(filePath)
            if (file.exists() && file.parentFile == photosDir) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get File object from path (for Coil)
     */
    fun getPhotoFile(filePath: String?): File? {
        if (filePath == null) return null
        val file = File(filePath)
        return if (file.exists()) file else null
    }
}