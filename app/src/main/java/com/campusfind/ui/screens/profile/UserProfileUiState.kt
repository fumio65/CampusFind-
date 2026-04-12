package com.campusfind.ui.screens.profile

import com.campusfind.domain.model.LostItem

data class UserProfileUiState(
    // User info
    val userName: String? = null,
    val userEmail: String? = null,
    val joinedDate: Long? = null,
    val messengerHandle: String? = null,
    val profilePhotoUri: String? = null,   // ← NEW: local file path or https:// url

    // User's items
    val items: List<LostItem> = emptyList(),

    // Trust score
    val trustScore: Int? = null,
    val trustScoreLabel: String? = null,
    val trustScoreRank: String? = null,

    // Phase 2 metrics
    val responseRate: String? = null,
    val avgReplyTime: String? = null,
    val recoveredRate: String? = null,

    // UI state
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,         // ← NEW: for save button loading state
    val error: String? = null,
    val saveSuccess: Boolean = false       // ← NEW: flash success after save
)