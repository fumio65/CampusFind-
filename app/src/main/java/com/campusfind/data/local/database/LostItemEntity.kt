package com.campusfind.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * FILE: app/src/main/java/com/campusfind/data/local/database/LostItemEntity.kt
 *
 * Room entity mapping to the `lost_items` table.
 *
 * UPDATED: Added location and photo_uri fields for better UX.
 *
 * New fields:
 * - location: nullable String — where the item was lost/found (e.g., "Near Library Entrance")
 * - photo_uri: nullable String — Android content URI pointing to the photo
 *
 * Why photo_uri instead of storing image bytes:
 * - Storing full image in Room would bloat the database
 * - URI points to the file in app's internal storage
 * - More efficient and follows Android best practices
 *
 * See: DEC-006 (Room), DEC-009 (UUIDs), DEC-010 (enum as String),
 *      DEC-011 (timestamps), DEC-015 (indexes), DEC-020 (multi-user shared DB), TASK-113
 */
@Entity(
    tableName = "lost_items",
    foreignKeys = [
        ForeignKey(
            entity        = UserEntity::class,
            parentColumns = ["id"],
            childColumns  = ["reported_by"],
            onDelete      = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["status"]),
        Index(value = ["reported_by"]),
        Index(value = ["reported_at"])
    ]
)
data class LostItemEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "location")
    val location: String? = null,              // NEW: "Near Library Entrance"

    @ColumnInfo(name = "photo_uri")
    val photoUri: String? = null,              // NEW: content://...

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "reported_by")
    val reportedBy: String,

    @ColumnInfo(name = "reported_at")
    val reportedAt: Long,

    @ColumnInfo(name = "last_modified_at")
    val lastModifiedAt: Long
)