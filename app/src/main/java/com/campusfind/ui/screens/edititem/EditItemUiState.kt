package com.campusfind.ui.screens.edititem

import android.net.Uri

data class EditItemUiState(
    val title: String = "",
    val description: String = "",
    val location: String = "",  // ✅ Location support
    val currentPhotoUri: String? = null,  // ✅ Current photo from DB (File path)
    val selectedPhotoUri: Uri? = null,  // ✅ New selected photo (content:// URI for preview)
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isItemLoaded: Boolean = false,  // ✅ Track if item loaded successfully
    val error: String? = null,
    val titleError: String? = null,
    val descriptionError: String? = null
)