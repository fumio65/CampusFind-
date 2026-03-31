package com.campusfind.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.preferences.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        // Read persisted dark mode on init so the toggle reflects reality on screen open
        SettingsUiState(isDarkMode = sessionManager.isDarkMode)
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleDarkMode() {
        val newValue = !_uiState.value.isDarkMode
        // 1. Persist to SharedPreferences so it survives app restart
        sessionManager.setDarkMode(newValue)
        // 2. Update UI state so the switch reacts immediately
        _uiState.update { it.copy(isDarkMode = newValue) }
    }

    fun toggleNotifications() {
        _uiState.update { it.copy(notificationsEnabled = !it.notificationsEnabled) }
        // TODO: Persist notifications preference in Phase 2
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
        }
    }
}