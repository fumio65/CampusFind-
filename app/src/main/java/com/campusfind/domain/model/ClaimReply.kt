package com.campusfind.domain.model

data class ClaimReply(
    val id: String,
    val claimId: String,
    val authorId: String,
    val authorName: String,
    val message: String,
    val createdAt: Long
)