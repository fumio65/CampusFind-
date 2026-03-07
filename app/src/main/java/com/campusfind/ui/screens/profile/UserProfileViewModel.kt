package com.campusfind.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.repository.LostItemRepository
import com.campusfind.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/profile/UserProfileViewModel.kt
 *
 * ViewModel for the user profile screen.
 *
 * Why @HiltViewModel:
 * - Hilt injects repositories and SessionManager automatically
 * - UserProfileScreen calls hiltViewModel() to get this instance
 *
 * Why loadProfile() loads both User and Items:
 * - Profile screen needs user info (name, email, joined date)
 * - Profile screen also shows user's posted items
 * - Both loaded in one coroutine to avoid sequential delays
 *
 * See: DEC-001 (MVVM), DEC-022 (Hilt DI), TASK-117
 */
@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val lostItemRepository: LostItemRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    /**
     * Load current user's profile and their posted items.
     *
     * Called by: UserProfileScreen in LaunchedEffect
     */
    fun loadProfile() {
        val userId = sessionManager.currentUserId
        if (userId == null) {
            _uiState.update {
                it.copy(error = "Not logged in")
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // Load user info
                val user = userRepository.getUserById(userId)
                if (user == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "User not found"
                        )
                    }
                    return@launch
                }

                // Load user's items from LostItemDao.getItemsByUser()
                // We need to collect the Flow once to get the current list
                lostItemRepository.getAllItems().collect { allItems ->
                    val userItems = allItems.filter { it.reportedBy == userId }

                    _uiState.update {
                        it.copy(
                            userName = user.fullName,
                            userEmail = user.email,
                            joinedDate = user.createdAt,
                            items = userItems,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load profile"
                    )
                }
            }
        }
    }
}