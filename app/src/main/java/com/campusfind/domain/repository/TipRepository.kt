package com.campusfind.domain.repository

import com.campusfind.domain.model.Tip
import kotlinx.coroutines.flow.Flow

interface TipRepository {
    fun getTipsByItemId(itemId: String): Flow<List<Tip>>
    suspend fun getTipCount(itemId: String): Int
    suspend fun submitTip(itemId: String, authorId: String, message: String): Result<Unit>
    suspend fun submitReply(parentTipId: String, itemId: String, authorId: String, message: String): Result<Unit>  // NEW
    suspend fun deleteTip(tipId: String): Result<Unit>
}