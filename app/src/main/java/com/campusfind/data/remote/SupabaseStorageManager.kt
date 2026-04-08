package com.campusfind.data.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val supabaseUrl = "https://pvzgeheitqdiehtlonzn.supabase.co"
    private val anonKey     = "sb_publishable_jY75mZiBm3tkZnMiA_gUzw_qTTDvCWh"
    private val bucket      = "item-photos"

    private val client = OkHttpClient()

    /**
     * Uploads a local image file to Supabase Storage.
     * Returns the public HTTPS URL, or null if upload failed.
     */
    suspend fun uploadPhoto(localFilePath: String): String? = withContext(Dispatchers.IO) {
        try {
            val file      = File(localFilePath)
            if (!file.exists()) return@withContext null

            val fileName  = "${UUID.randomUUID()}.jpg"
            val uploadUrl = "$supabaseUrl/storage/v1/object/$bucket/$fileName"

            val body = file.readBytes().toRequestBody("image/jpeg".toMediaType())

            val request = Request.Builder()
                .url(uploadUrl)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "image/jpeg")
                .addHeader("x-upsert", "true")
                .put(body)
                .build()

            val response = client.newCall(request).execute()

            return@withContext if (response.isSuccessful) {
                val publicUrl = "$supabaseUrl/storage/v1/object/public/$bucket/$fileName"
                android.util.Log.d("Supabase", "✅ Upload success: $publicUrl")
                publicUrl
            } else {
                val errorBody = response.body?.string()
                android.util.Log.e("Supabase", "❌ Upload failed: HTTP ${response.code} — $errorBody")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletes a photo from Supabase Storage given its public URL.
     */
    suspend fun deletePhoto(publicUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Extract file name from URL
            val fileName  = publicUrl.substringAfterLast("/")
            val deleteUrl = "$supabaseUrl/storage/v1/object/$bucket/$fileName"

            val request = Request.Builder()
                .url(deleteUrl)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("apikey", anonKey)
                .delete()
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}