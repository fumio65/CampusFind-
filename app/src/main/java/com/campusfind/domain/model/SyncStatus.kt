package com.campusfind.domain.model

/**
 * FILE: app/src/main/java/com/campusfind/domain/model/SyncStatus.kt
 *
 * Sync status for an item — Phase 2 only.
 *
 * Phase 1: This enum exists but is NOT used anywhere yet.
 * Phase 2: Added to LostItem domain model when sync is implemented (TASK-206).
 *
 * Why it exists now:
 * - Reserved for Phase 2 architecture
 * - Demonstrates forward-thinking design
 * - No harm in having it unused for Phase 1
 *
 * Values:
 * - SYNCED        — item exists in both Room and Firestore, fully synchronized
 * - PENDING_SYNC  — item created/modified locally, not yet uploaded to Firestore
 * - SYNC_FAILED   — upload to Firestore failed, will retry on next sync attempt
 *
 * See: DEC-010 (enum storage), TASK-111, TASK-206 (Phase 2 sync)
 */
enum class SyncStatus {
    SYNCED,
    PENDING_SYNC,
    SYNC_FAILED;

    companion object {
        fun fromString(value: String): SyncStatus {
            return valueOf(value.uppercase())
        }
    }
}