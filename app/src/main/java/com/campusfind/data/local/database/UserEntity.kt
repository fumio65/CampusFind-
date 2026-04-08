package com.campusfind.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.campusfind.domain.model.SyncStatus

/**
 * FILE: app/src/main/java/com/campusfind/data/local/database/UserEntity.kt
 *
 * Room entity mapping to the `users` table.
 *
 * Why this structure:
 * - id is a UUID String generated offline (DEC-009) — no server needed
 * - email has a UNIQUE index so duplicate registrations are rejected at DB level (DEC-015)
 * - password_hash stores SHA-256 hash, never plain text (DEC-016)
 * - messenger_handle is nullable — it is optional at registration (PRD MCO1 Feature #1)
 * - created_at is a Long (Unix epoch ms) for consistent cross-platform timestamps (DEC-011)
 *
 * See: DEC-006 (Room), DEC-009 (UUIDs), DEC-010 (enum storage),
 *      DEC-011 (timestamps), DEC-015 (indexes), DEC-016 (local auth)
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true)   // fast login lookup + prevents duplicate accounts
    ]
)
data class UserEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,                             // UUID v4 — generated via UUID.randomUUID()

    @ColumnInfo(name = "full_name")
    val fullName: String,

    @ColumnInfo(name = "email")
    val email: String,                          // unique — enforced by index above

    @ColumnInfo(name = "password_hash")
    val passwordHash: String,                   // SHA-256 hash — never store plain text

    @ColumnInfo(name = "messenger_handle")
    val messengerHandle: String? = null,        // optional @ username for Phase 2 deep link

    @ColumnInfo(name = "created_at")
    val createdAt: Long,                        // System.currentTimeMillis()

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = SyncStatus.PENDING_SYNC.name
)