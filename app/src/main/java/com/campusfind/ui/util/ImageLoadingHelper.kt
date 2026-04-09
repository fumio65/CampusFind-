package com.campusfind.ui.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import java.io.File

/**
 * Resolves a photoUri (local file path OR https:// URL) into a Coil
 * ImageRequest with correct cache settings.
 *
 * - Local file path: loaded from disk, no network needed (offline-first)
 * - https:// URL: fetched from Supabase CDN, disk-cached by Coil
 *
 * The memoryCacheKey is set explicitly to the URI string so Coil
 * invalidates its in-memory cache whenever the URI changes (e.g. after
 * a photo edit produces a new unique URL).
 */
@Composable
fun rememberItemImagePainter(photoUri: String?) = rememberAsyncImagePainter(
    model = buildImageRequest(LocalContext.current, photoUri)
)

fun buildImageRequest(context: Context, photoUri: String?): ImageRequest? {
    val model: Any = when {
        photoUri.isNullOrBlank()          -> return null
        photoUri.startsWith("https://")   -> photoUri
        else -> File(photoUri).takeIf { it.exists() } ?: return null
    }

    return ImageRequest.Builder(context)
        .data(model)
        // Use the full URI as the memory cache key so a new URL always
        // results in a cache miss and a fresh network fetch.
        .memoryCacheKey(photoUri)
        .diskCacheKey(photoUri)
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .crossfade(true)
        .build()
}