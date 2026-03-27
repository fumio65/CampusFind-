package com.campusfind.ui.screens.detail

import com.campusfind.domain.model.Claim
import com.campusfind.domain.model.ClaimReply
import com.campusfind.domain.model.LostItem
import com.campusfind.domain.model.Tip

data class DetailUiState(
    val item: LostItem? = null,
    val reporterName: String? = null,
    val isOwner: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val tips: List<Tip> = emptyList(),
    val tipCount: Int = 0,
    val hasUserTipped: Boolean = false,
    val showLimitDialog: String? = null,
    val claims: List<Claim> = emptyList(),
    val claimCount: Int = 0,
    val claimReplies: Map<String, List<ClaimReply>> = emptyMap(),
    val claimReplyCounts: Map<String, Int> = emptyMap()
)