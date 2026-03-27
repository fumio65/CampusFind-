// domain/model/Claim.kt
package com.campusfind.domain.model

data class Claim(
    val id: String,
    val itemId: String,
    val claimerId: String,
    val claimerName: String,
    val message: String,
    val photoUri: String?,
    val claimedAt: Long,
    val status: ClaimStatus,
    val messengerUsername: String? = null  // ✅ NEW: Finder's messenger handle for deep link
)

enum class ClaimStatus {
    PENDING,
    APPROVED,
    REJECTED;

    companion object {
        fun fromString(value: String): ClaimStatus {
            return valueOf(value.uppercase())
        }
    }
}