package com.campusfind.ui.screens.reviewclaims

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.domain.model.Claim
import com.campusfind.domain.model.ClaimStatus
import com.campusfind.domain.repository.ClaimRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/reviewclaims/ReviewClaimsViewModel.kt
 *
 * ViewModel for ReviewClaimsScreen.
 *
 * Item owner can:
 * - View all claims on their item
 * - See claimer name + message + photo
 * - Approve claim (confirms it's their item)
 * - Reject claim (not their item)
 * - Delete spam claims
 *
 * See: DEC-022 (Hilt ViewModel), Phase 6 Claims Feature
 */

data class ReviewClaimsUiState(
    val claims: List<Claim> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedClaimId: String? = null  // For approve/reject confirmation
)

@HiltViewModel
class ReviewClaimsViewModel @Inject constructor(
    private val claimRepository: ClaimRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewClaimsUiState())
    val uiState: StateFlow<ReviewClaimsUiState> = _uiState.asStateFlow()

    /**
     * Load all claims for an item.
     */
    fun loadClaims(itemId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                claimRepository.getClaimsByItem(itemId).collect { claims ->
                    _uiState.update {
                        it.copy(
                            claims = claims,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load claims: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Approve a claim.
     */
    fun approveClaim(claimId: String) {
        viewModelScope.launch {
            val result = claimRepository.updateClaimStatus(claimId, ClaimStatus.APPROVED)

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(selectedClaimId = null) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(error = "Failed to approve: ${error.message}")
                    }
                }
            )
        }
    }

    /**
     * Reject a claim.
     */
    fun rejectClaim(claimId: String) {
        viewModelScope.launch {
            val result = claimRepository.updateClaimStatus(claimId, ClaimStatus.REJECTED)

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(selectedClaimId = null) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(error = "Failed to reject: ${error.message}")
                    }
                }
            )
        }
    }

    /**
     * Delete a claim (spam/invalid).
     */
    fun deleteClaim(claimId: String) {
        viewModelScope.launch {
            val result = claimRepository.deleteClaim(claimId)

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(selectedClaimId = null) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(error = "Failed to delete: ${error.message}")
                    }
                }
            )
        }
    }
}