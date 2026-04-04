package com.campusfind.domain.model

data class Claim(
    val id: String,
    val itemId: String,
    val claimerId: String,
    val claimerName: String,
    val message: String,
    val photoUri: String?,          // first photo — backward compat
    val photoUris: List<String> = emptyList(), // all photos — NEW
    val claimedAt: Long,
    val status: ClaimStatus,
    val messengerUsername: String? = null
)

enum class ClaimStatus {
    PENDING,
    APPROVED,
    REJECTED;

    companion object {
        fun fromString(value: String): ClaimStatus = valueOf(value.uppercase())
    }
}