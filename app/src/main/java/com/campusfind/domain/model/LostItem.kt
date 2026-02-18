package com.campusfind.domain.model

/**
 * FILE: app/src/main/java/com/campusfind/domain/model/LostItem.kt
 *
 * Domain model representing a lost or found item.
 * Pure Kotlin data class — no Room annotations, no Android dependencies.
 *
 * Why this exists separate from LostItemEntity:
 * - LostItemEntity is a data layer concern — it has @Entity, @ColumnInfo, FK constraints
 * - LostItem is a domain model — ViewModels and UseCases work with this, never with LostItemEntity
 * - This separation satisfies DIP — the domain layer has zero dependency on Room
 *
 * Why reportedAt and lastModifiedAt are Long:
 * - Unix epoch milliseconds (System.currentTimeMillis())
 * - Easy to compare, sort, and format for display
 * - Cross-platform compatible (DEC-011)
 *
 * Phase 2 additions (TASK-206):
 * - syncStatus: SyncStatus (SYNCED, PENDING_SYNC, SYNC_FAILED)
 * - lastSyncedAt: Long? (timestamp of last successful Firestore sync)
 *
 * See: DEC-001 (MVVM), DEC-002 (Repository), DEC-009 (UUID),
 *      DEC-010 (enum storage), DEC-011 (timestamps), DEC-022 (DIP), TASK-111
 */
data class LostItem(
    val id: String,                     // UUID v4
    val title: String,
    val description: String,
    val status: ItemStatus,             // LOST or FOUND
    val reportedBy: String,             // userId (FK → users.id in Room)
    val reportedAt: Long,               // System.currentTimeMillis()
    val lastModifiedAt: Long            // updated on every status change
)