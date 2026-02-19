package com.campusfind.ui.screens.additem

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/additem/AddItemUiState.kt
 *
 * UI state for the add item screen.
 *
 * UPDATED: Added location and photoUri fields.
 *
 * See: DEC-001 (MVVM), TASK-113
 */
data class AddItemUiState(
    val title: String = "",
    val description: String = "",
    val location: String = "",         // NEW: "Near Library Entrance"
    val photoUri: String? = null,      // NEW: content://... (nullable until photo is picked)
    val isSubmitting: Boolean = false,
    val error: String? = null
)