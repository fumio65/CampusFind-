package com.campusfind.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.photo.PhotoManager
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.data.sync.SyncManager
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import com.campusfind.domain.repository.LostItemRepository
import com.campusfind.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val lostItemRepository: LostItemRepository,
    private val sessionManager: SessionManager,
    private val syncManager: SyncManager,
    private val photoManager: PhotoManager
) : ViewModel() {

    private val _extraState = MutableStateFlow(ExtraState())
    private data class ExtraState(
        val isSaving: Boolean = false,
        val saveSuccess: Boolean = false,
        val error: String? = null
    )

    // Build the entire UiState reactively from Room Flows.
    // This Flow runs for the entire ViewModel lifetime — it is NEVER cancelled
    // by loadProfile() calls or any other function. Any Room write to the user
    // row or items table immediately triggers a new emission and the UI updates.
    val uiState: StateFlow<UserProfileUiState> = run {
        val userId = sessionManager.currentUserId

        if (userId == null) {
            MutableStateFlow(UserProfileUiState(error = "Not logged in"))
        } else {
            combine(
                userRepository.observeUser(userId)
                    .onEach { android.util.Log.d("PROFILE_DEBUG", "observeUser emitted: photo=${it?.profilePhotoUri?.takeLast(30)}") }
                    .filterNotNull(),
                lostItemRepository.getItemsByUser(userId),
                _extraState
            ) { user, items, extra ->
                android.util.Log.d("PROFILE_DEBUG", "combine fired: photo=${user.profilePhotoUri?.takeLast(30)}")
                val trustScore      = calculateTrustScore(items)
                val foundItemsCount = items.count { it.status == ItemStatus.FOUND }
                UserProfileUiState(
                    userName        = user.fullName,
                    userEmail       = user.email,
                    joinedDate      = user.createdAt,
                    messengerHandle = user.messengerHandle,
                    profilePhotoUri = user.profilePhotoUri,
                    items           = items,
                    trustScore      = trustScore,
                    trustScoreLabel = getTrustScoreLabel(trustScore),
                    trustScoreRank  = getTrustScoreRank(trustScore),
                    responseRate    = if (foundItemsCount > 0) "100%" else null,
                    avgReplyTime    = if (foundItemsCount > 0) "3.2h" else null,
                    recoveredRate   = if (items.isNotEmpty()) "$foundItemsCount/${items.size}" else null,
                    isLoading       = false,
                    isSaving        = extra.isSaving,
                    saveSuccess     = extra.saveSuccess,
                    error           = extra.error
                )
            }
                .catch { e ->
                    emit(UserProfileUiState(error = e.message ?: "Failed to load profile"))
                }
                .stateIn(
                    scope        = viewModelScope,
                    started      = SharingStarted.Eagerly,
                    initialValue = UserProfileUiState(isLoading = true)
                )
        }
    }

    // loadProfile is now a no-op — the Flow above handles everything reactively.
    // Kept for call-site compatibility (LaunchedEffect(Unit) { viewModel.loadProfile() }).
    fun loadProfile() { /* reactive — no action needed */ }

    fun refresh() {
        syncManager.triggerNow()
    }

    fun updateProfile(fullName: String, email: String) {
        val userId = sessionManager.currentUserId ?: return
        viewModelScope.launch {
            _extraState.update { it.copy(isSaving = true) }
            try {
                val result = userRepository.updateProfile(userId, fullName.trim(), email.trim())
                _extraState.update { it.copy(isSaving = false) }
                if (result.isSuccess) {
                    _extraState.update { it.copy(saveSuccess = true) }
                    delay(2000)
                    _extraState.update { it.copy(saveSuccess = false) }
                } else {
                    _extraState.update { it.copy(error = result.exceptionOrNull()?.message ?: "Failed to update profile") }
                }
            } catch (e: Exception) {
                _extraState.update { it.copy(isSaving = false, error = "Failed to update: ${e.message}") }
            }
        }
    }

    fun updateProfilePhoto(photoUri: String) {
        val userId = sessionManager.currentUserId ?: return
        viewModelScope.launch {
            try {
                android.util.Log.d("PROFILE_DEBUG", "updateProfilePhoto: input uri=$photoUri")
                val stablePath = if (photoUri.startsWith("content://")) {
                    val saved = photoManager.savePhoto(Uri.parse(photoUri))
                    android.util.Log.d("PROFILE_DEBUG", "savePhoto result: $saved")
                    saved ?: run {
                        _extraState.update { it.copy(error = "Could not read photo") }
                        return@launch
                    }
                } else {
                    photoUri
                }
                android.util.Log.d("PROFILE_DEBUG", "stablePath=$stablePath exists=${java.io.File(stablePath).exists()}")
                val result = userRepository.updateProfilePhoto(userId, stablePath)
                android.util.Log.d("PROFILE_DEBUG", "updateProfilePhoto result: success=${result.isSuccess}")
                if (result.isFailure) {
                    _extraState.update { it.copy(error = result.exceptionOrNull()?.message ?: "Failed to update photo") }
                }
            } catch (e: Exception) {
                android.util.Log.e("PROFILE_DEBUG", "updateProfilePhoto EXCEPTION: ${e.message}", e)
                _extraState.update { it.copy(error = "Failed to update photo: ${e.message}") }
            }
        }
    }

    fun updateMessengerHandle(handle: String) {
        val userId = sessionManager.currentUserId ?: return
        viewModelScope.launch {
            try {
                val result = userRepository.updateMessengerHandle(userId, handle)
                if (result.isFailure) {
                    _extraState.update { it.copy(error = result.exceptionOrNull()?.message ?: "Failed to update Messenger") }
                }
            } catch (e: Exception) {
                _extraState.update { it.copy(error = "Failed to update Messenger: ${e.message}") }
            }
        }
    }

    fun clearError() { _extraState.update { it.copy(error = null) } }

    private fun calculateTrustScore(items: List<LostItem>): Int {
        if (items.isEmpty()) return 0
        return (50 + items.size * 5 + items.count { it.status == ItemStatus.FOUND } * 15).coerceIn(0, 100)
    }

    private fun getTrustScoreLabel(score: Int) = when {
        score == 0  -> "New User"
        score >= 90 -> "Excellent Trust Score"
        score >= 70 -> "Good Trust Score"
        score >= 50 -> "Fair Trust Score"
        else        -> "Building Trust Score"
    }

    private fun getTrustScoreRank(score: Int) = when {
        score == 0  -> "Get started by posting items!"
        score >= 95 -> "Top 5% of campus users"
        score >= 85 -> "Top 15% of campus users"
        score >= 70 -> "Top 30% of campus users"
        else        -> "Keep going!"
    }
}