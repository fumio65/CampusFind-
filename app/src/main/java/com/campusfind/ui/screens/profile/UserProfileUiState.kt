package com.campusfind.ui.screens.profile

import com.campusfind.domain.model.LostItem

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/profile/UserProfileUiState.kt
 *
 * UI state for the user profile screen.
 *
 * Why separate from User domain model:
 * - Combines User data + their posted items in one state
 * - UI-specific concerns (loading, error) don't belong in domain model
 *
 * See: DEC-001 (MVVM), TASK-117
 */
data class UserProfileUiState(
    val userName: String? = null,
    val userEmail: String? = null,
    val joinedDate: Long? = null,
    val items: List<LostItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)