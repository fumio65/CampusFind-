package com.campusfind.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TipDto(
    val id: String,
    @SerialName("item_id") val itemId: String,
    @SerialName("author_id") val authorId: String,
    val message: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("parent_tip_id") val parentTipId: String? = null
)
