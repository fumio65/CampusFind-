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
 * Why this structure:
 * - reported_by is a FK → users.id so every item is always linked to a real user (DEC-020)
 * - status is stored as a String ("LOST" / "FOUND") not an Int — readable in DB inspection,
 *   safe from enum reordering (DEC-010)
 * - Indexes on status, reported_by, reported_at — these are the three most queried columns:
 *     status       → filter chips (All/Lost/Found)
 *     reported_by  → ownership checks (canMarkAsFound, canDelete)
 *     reported_at  → default sort order DESC (DEC-015)
 * - ForeignKey onDelete = CASCADE — if a user is deleted, their items are deleted too
 *
 * Phase 2 additions (do NOT add yet — wait for TASK-206):
 *   sync_status    String  DEFAULT 'PENDING_SYNC'
 *   last_synced_at Long?
 *
 * See: DEC-006 (Room), DEC-009 (UUIDs), DEC-010 (enum as String),
 *      DEC-011 (timestamps), DEC-015 (indexes), DEC-020 (multi-user shared DB)
 */
@Entity(
    tableName = "lost_items",
    foreignKeys = [
        ForeignKey(
            entity        = UserEntity::class,
            parentColumns = ["id"],
            childColumns  = ["reported_by"],
            onDelete      = ForeignKey.CASCADE   // delete user → delete their items
        )
    ],
    indices = [
        Index(value = ["status"]),               // filter chips query
        Index(value = ["reported_by"]),          // ownership check query
        Index(value = ["reported_at"])           // default sort DESC
    ]
)
data class LostItemEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,                             // UUID v4 — generated via UUID.randomUUID()

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "status")
    val status: String,                         // "LOST" or "FOUND" — stored as String (DEC-010)

    @ColumnInfo(name = "reported_by")
    val reportedBy: String,                     // FK → users.id

    @ColumnInfo(name = "reported_at")
    val reportedAt: Long,                       // System.currentTimeMillis()

    @ColumnInfo(name = "last_modified_at")
    val lastModifiedAt: Long                    // updated on every status change
)