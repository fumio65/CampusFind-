package com.campusfind.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.campusfind.domain.model.SyncStatus
import java.util.UUID

@Entity(tableName = "lost_items")
data class LostItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "location")  // ✅ NEW: Location field
    val location: String? = null,

    @ColumnInfo(name = "status")
    val status: String, // "LOST", "FOUND"

    @ColumnInfo(name = "reported_by")
    val reportedBy: String,

    @ColumnInfo(name = "reported_at")
    val reportedAt: Long,

    @ColumnInfo(name = "last_modified_at")
    val lastModifiedAt: Long,

    @ColumnInfo(name = "photo_uri")
    val photoUri: String? = null,

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = SyncStatus.PENDING_SYNC.name
)