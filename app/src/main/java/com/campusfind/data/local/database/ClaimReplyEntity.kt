package com.campusfind.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.campusfind.domain.model.SyncStatus
import java.util.UUID

@Entity(
    tableName = "claim_replies",
    foreignKeys = [
        ForeignKey(
            entity = ClaimEntity::class,
            parentColumns = ["id"],
            childColumns = ["claim_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["author_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["claim_id"]),
        Index(value = ["author_id"]),
        Index(value = ["created_at"])
    ]
)
data class ClaimReplyEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "claim_id")
    val claimId: String,

    @ColumnInfo(name = "author_id")
    val authorId: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = SyncStatus.PENDING_SYNC.name
)