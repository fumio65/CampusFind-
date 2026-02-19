package com.campusfind.ui.screens.detail

import com.campusfind.domain.model.LostItem

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/detail/DetailUiState.kt
 *
 * UI state for the detail screen.
 *
 * Why isOwner is computed here (not in UI):
 * - Ownership check is business logic, not presentation logic
 * - ViewModel compares item.reportedBy == sessionManager.currentUserId
 * - UI receives a boolean flag and renders accordingly
 * - Prevents UI from accidentally showing controls to non-owners
 *
 * Why reporterName is separate:
 * - Loaded from UserRepository.getUserById()
 * - Displayed as "Reported by: [name]"
 * - If user was deleted (CASCADE), falls back to "Unknown User"
 *
 * See: DEC-001 (MVVM), DEC-021 (ownership enforcement in ViewModel),
 *      TASK-114, demo steps 10, 13, 14
 */
data class DetailUiState(
    val item: LostItem? = null,
    val reporterName: String? = null,
    val isOwner: Boolean = false,          // ViewModel sets this, UI reads it
    val isLoading: Boolean = false,
    val error: String? = null
)