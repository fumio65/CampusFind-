package com.campusfind.domain.model

data class LostItem(
    val id: String,
    val title: String,
    val description: String,
    val location: String? = null,
    val status: ItemStatus,
    val reportedBy: String,
    val reportedAt: Long,
    val lastModifiedAt: Long,
    val photoUri: String? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED  // ← added
)