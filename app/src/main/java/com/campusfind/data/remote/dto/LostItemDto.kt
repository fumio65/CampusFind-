package com.campusfind.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LostItemDto(
    val id: String,
    val title: String,
    val description: String,
    val location: String? = null,
    val status: String,
    @SerialName("reported_by") val reportedBy: String,
    @SerialName("reported_at") val reportedAt: Long,
    @SerialName("last_modified_at") val lastModifiedAt: Long,
    @SerialName("photo_uri") val photoUri: String? = null
)
