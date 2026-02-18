package com.campusfind.domain.model

/**
 * FILE: app/src/main/java/com/campusfind/domain/model/ItemStatus.kt
 *
 * Status of a lost/found item.
 *
 * Why enum instead of String constants:
 * - Type-safe — compiler prevents typos like "LOSTT"
 * - Exhaustive when expressions — compiler ensures all cases are handled
 * - Clear domain vocabulary
 *
 * Why stored as String in Room (DEC-010):
 * - enum.name returns "LOST" or "FOUND" as a String
 * - Stored in database as varchar — readable in DB inspection tools
 * - Safe from enum reordering bugs (if we add RESOLVED later, LOST stays "LOST")
 *
 * Phase 1: LOST, FOUND
 * Phase 2: add RESOLVED (when reporter confirms item was returned)
 *
 * See: DEC-010 (enum storage), TASK-111
 */
enum class ItemStatus {
    LOST,
    FOUND;
    // Phase 2: add RESOLVED here (TASK-206)

    companion object {
        /**
         * Convert a String from the database back to an enum.
         * Throws IllegalArgumentException if the string doesn't match any enum value.
         */
        fun fromString(value: String): ItemStatus {
            return valueOf(value.uppercase())
        }
    }
}