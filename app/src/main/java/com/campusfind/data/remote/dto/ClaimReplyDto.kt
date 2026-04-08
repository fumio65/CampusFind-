package com.campusfind.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClaimReplyDto(
    val id: String,
    @SerialName("claim_id") val claimId: String,
    @SerialName("author_id") val authorId: String,
    val message: String,
    @SerialName("created_at") val createdAt: Long
)
