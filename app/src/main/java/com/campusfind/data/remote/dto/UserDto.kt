package com.campusfind.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id")                val id: String,
    @SerialName("full_name")         val fullName: String,
    @SerialName("email")             val email: String,
    @SerialName("messenger_handle")  val messengerHandle: String? = null,
    @SerialName("created_at")        val createdAt: Long,
    @SerialName("password_hash")     val passwordHash: String = "",
    @SerialName("profile_photo_uri") val profilePhotoUri: String? = null  // ← this line
)