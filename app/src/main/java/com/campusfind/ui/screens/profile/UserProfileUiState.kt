package com.campusfind.ui.screens.profile

import com.campusfind.domain.model.LostItem

/**
 * UserProfileUiState - COMPLETE WITH ALL FIELDS
 *
 * UI state for the user profile screen.
 *
 * Why separate from User domain model:
 * - Combines User data + their posted items in one state
 * - UI-specific concerns (loading, error) don't belong in domain model
 * - Includes computed fields (trust score, stats) for display
 *
 * See: DEC-001 (MVVM), TASK-117
 */
data class UserProfileUiState(
    // ══════════════════════════════════════
    // USER INFO
    // ══════════════════════════════════════
    val userName: String? = null,
    val userEmail: String? = null,
    val joinedDate: Long? = null,
    val messengerHandle: String? = null,

    // ══════════════════════════════════════
    // USER'S ITEMS
    // ══════════════════════════════════════
    val items: List<LostItem> = emptyList(),

    // ══════════════════════════════════════
    // TRUST SCORE & REPUTATION
    // ══════════════════════════════════════
    val trustScore: Int? = null,
    val trustScoreLabel: String? = null,
    val trustScoreRank: String? = null,

    // Phase 2 metrics
    val responseRate: String? = null,      // e.g., "95%"
    val avgReplyTime: String? = null,      // e.g., "3.2h"
    val recoveredRate: String? = null,     // e.g., "4/7"

    // ══════════════════════════════════════
    // UI STATE
    // ══════════════════════════════════════
    val isLoading: Boolean = false,
    val error: String? = null
)