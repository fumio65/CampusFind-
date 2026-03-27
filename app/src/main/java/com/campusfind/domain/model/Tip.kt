package com.campusfind.domain.model

data class Tip(
    val id: String,
    val itemId: String,
    val authorId: String,
    val authorName: String,
    val message: String,
    val createdAt: Long,
    val parentTipId: String? = null,  // NEW: null = top-level, non-null = reply
    val isReply: Boolean = false       // NEW: convenience flag
)