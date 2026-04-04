package com.campusfind.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * FILE: app/src/main/java/com/campusfind/data/local/database/ClaimEntity.kt
 *
 * Room entity for claims table.
 *
 * What is a claim?
 * - When User B finds User A's lost item, User B submits a "claim"
 * - Claim includes: message + photo proof (stored locally)
 * - User A (the reporter) can approve/reject the claim
 * - If approved → User A can contact User B
 *
 * Why foreign keys with CASCADE?
 * - When an item is deleted, all its claims are automatically deleted
 * - When a user is deleted, all their claims are de'leted
 * - Maintains referential integrity
 *
 * Photo storage:
 * - photoUri stores local file path: /data/user/0/.../files/claims/claim_abc123.jpg
 * - Photos saved via PhotoManager, not directly in database
 *
 * See: DEC-009 (UUID), DEC-011 (timestamps), Phase 6 Claims Feature
 */
@Entity(
    tableName = "claims",
    foreignKeys = [
        ForeignKey(
            entity = LostItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["claimed_by"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["item_id"]),
        Index(value = ["claimed_by"]),
        Index(value = ["status"])
    ]
)
data class ClaimEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "item_id")
    val itemId: String,

    @ColumnInfo(name = "claimed_by")
    val claimedBy: String,  // User who claims they found it

    @ColumnInfo(name = "message")
    val message: String,  // "I found this in the library near the computers"

    @ColumnInfo(name = "photo_uri")
    val photoUri: String?,  // Local file path, nullable (photo is optional)

    @ColumnInfo(name = "status")
    val status: String,  // "PENDING", "APPROVED", "REJECTED"

    @ColumnInfo(name = "claimed_at")
    val claimedAt: Long,  // When the claim was submitted

    @ColumnInfo(name = "reviewed_at")
    val reviewedAt: Long? = null  // When owner approved/rejected (null = pending)
)