package com.campusfind.ui.screens.additem

import android.net.Uri

data class AddItemUiState(
    val title: String = "",
    val description: String = "",
    val location: String = "",  // ✅ NEW: Location field
    val photoUri: Uri? = null,
    val isSubmitting: Boolean = false,
    val error: String? = null
)