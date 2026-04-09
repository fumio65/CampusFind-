package com.campusfind.data.local.photo

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

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
     * Copy a content:// URI to internal storage and return the stable file path.
     *
     * Returns null ONLY if the content resolver cannot open the stream at all
     * (e.g. the URI was already revoked). Any other failure throws so the caller
     * can surface a real error instead of silently writing null to Room.
     *
     * Must be called from a coroutine — runs on Dispatchers.IO internally.
     */
    suspend fun savePhoto(uri: Uri): String? = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return@withContext null   // URI genuinely unreadable

        val filename   = "${UUID.randomUUID()}.jpg"
        val outputFile = File(photosDir, filename)

        // Let any IOException propagate to the caller (EditItemViewModel.onSave)
        // so it lands in the catch block and shows an error instead of
        // silently setting photo_uri = null in Room.
        FileOutputStream(outputFile).use { output ->
            inputStream.use { it.copyTo(output) }
        }

        outputFile.absolutePath
    }

    /**
     * Delete a local photo file. Skips https:// URLs — those live in Supabase
     * Storage and are not managed here.
     */
    fun deletePhoto(filePath: String?) {
        if (filePath.isNullOrBlank()) return
        if (filePath.startsWith("https://")) return   // never delete remote URLs
        try {
            val file = File(filePath)
            if (file.exists() && file.parentFile?.canonicalPath == photosDir.canonicalPath) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPhotoFile(filePath: String?): File? {
        if (filePath == null) return null
        val file = File(filePath)
        return if (file.exists()) file else null
    }
}