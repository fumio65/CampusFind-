package com.campusfind.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val email: String,
    @SerialName("messenger_handle") val messengerHandle: String? = null,
    @SerialName("created_at") val createdAt: Long
)
