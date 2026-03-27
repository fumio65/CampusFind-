package com.campusfind.ui.screens.submitclaim

import android.net.Uri

/**
 * UI State for Submit Claim Screen
 */
data class SubmitClaimUiState(
    val itemId: String = "",
    val itemTitle: String = "",
    val location: String = "",
    val photoUris: List<Uri> = emptyList(),  // Changed from photoUri to photoUris (max 3)
    val isSubmitting: Boolean = false,
    val error: String? = null
)