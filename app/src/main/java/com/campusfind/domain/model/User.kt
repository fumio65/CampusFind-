package com.campusfind.domain.model

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val messengerHandle: String?,
    val createdAt: Long,
    val profilePhotoUri: String? = null   // ← NEW: local path or https:// url
)