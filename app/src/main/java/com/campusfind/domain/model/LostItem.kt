package com.campusfind.domain.model

/**
 * FILE: app/src/main/java/com/campusfind/domain/model/LostItem.kt
 *
 * Domain model representing a lost or found item.
 *
 * UPDATED: Added location and photoUri fields for better UX.
 *
 * New fields:
 * - location: nullable String — where the item was lost/found
 * - photoUri: nullable String — URI pointing to the photo in device storage
 *
 * See: DEC-001 (MVVM), DEC-002 (Repository), DEC-009 (UUID),
 *      DEC-010 (enum storage), DEC-011 (timestamps), DEC-022 (DIP), TASK-111, TASK-113
 */
data class LostItem(
    val id: String,
    val title: String,
    val description: String,
    val location: String? = null,       // NEW: "Near Library Entrance"
    val photoUri: String? = null,       // NEW: content://...
    val status: ItemStatus,
    val reportedBy: String,
    val reportedAt: Long,
    val lastModifiedAt: Long
)