package com.campusfind.ui.screens.edititem

import android.net.Uri

data class EditItemUiState(
    val title            : String  = "",
    val description      : String  = "",
    val location         : String  = "",
    val currentPhotoUri  : String? = null,   // what's in Room — local path OR https:// URL
    val selectedPhotoUri : Uri?    = null,   // new photo just picked from gallery
    val photoRemoved     : Boolean = false,  // true only when user tapped the ✕ remove button
    val isLoading        : Boolean = false,
    val isSaving         : Boolean = false,
    val isItemLoaded     : Boolean = false,
    val error            : String? = null,
    val titleError       : String? = null,
    val descriptionError : String? = null
)