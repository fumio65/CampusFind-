package com.campusfind.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClaimDto(
    val id: String,
    @SerialName("item_id") val itemId: String,
    @SerialName("claimed_by") val claimedBy: String,
    val message: String,
    @SerialName("photo_uri") val photoUri: String? = null,
    val status: String,
    @SerialName("claimed_at") val claimedAt: Long,
    @SerialName("reviewed_at") val reviewedAt: Long? = null
)
