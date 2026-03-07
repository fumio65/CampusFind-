package com.campusfind.ui.screens.home

import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/home/HomeUiState.kt
 *
 * UI state for HomeScreen.
 *
 * NEW in this version:
 * - reporterNames: Map<String, String> — userId -> reporter full name
 * - Loaded in HomeViewModel after items are fetched
 * - Used by ModernItemCard to display actual reporter names instead of "Reporter"
 */
data class HomeUiState(
    val items: List<LostItem> = emptyList(),
    val reporterNames: Map<String, String> = emptyMap(),  // ← NEW: userId -> full name
    val selectedFilter: ItemStatus? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)