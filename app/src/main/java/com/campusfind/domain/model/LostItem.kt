package com.campusfind.domain.model

data class LostItem(
    val id: String,
    val title: String,
    val description: String,
    val location: String? = null,  // ✅ NEW: Location field
    val status: ItemStatus,
    val reportedBy: String,
    val reportedAt: Long,
    val lastModifiedAt: Long,
    val photoUri: String? = null
)