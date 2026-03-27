package com.campusfind.data.repository

import com.campusfind.data.local.database.TipDao
import com.campusfind.data.local.database.TipEntity
import com.campusfind.data.local.database.TipWithAuthor
import com.campusfind.domain.model.Tip
import com.campusfind.domain.repository.TipRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class TipRepositoryImpl @Inject constructor(
    private val tipDao: TipDao
) : TipRepository {

    override fun getTipsByItemId(itemId: String): Flow<List<Tip>> {
        return tipDao.getTipsByItemId(itemId).map { tipList ->
            tipList.map { it.toDomain() }
        }
    }

    override suspend fun getTipCount(itemId: String): Int {
        return tipDao.getTipCount(itemId)
    }

    override suspend fun submitTip(itemId: String, authorId: String, message: String): Result<Unit> {
        return try {
            val tip = TipEntity(
                id = UUID.randomUUID().toString(),
                itemId = itemId,
                authorId = authorId,
                message = message.trim(),
                createdAt = System.currentTimeMillis(),
                parentTipId = null  // Top-level tip
            )
            tipDao.insertTip(tip)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitReply(
        parentTipId: String,
        itemId: String,
        authorId: String,
        message: String
    ): Result<Unit> {
        return try {
            val reply = TipEntity(
                id = UUID.randomUUID().toString(),
                itemId = itemId,
                authorId = authorId,
                message = message.trim(),
                createdAt = System.currentTimeMillis(),
                parentTipId = parentTipId  // Reply to this tip
            )
            tipDao.insertTip(reply)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTip(tipId: String): Result<Unit> {
        return try {
            // Note: CASCADE will delete all replies automatically
            val tip = TipEntity(
                id = tipId,
                itemId = "",
                authorId = "",
                message = "",
                createdAt = 0
            )
            tipDao.deleteTip(tip)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun TipWithAuthor.toDomain(): Tip {
        return Tip(
            id = tip.id,
            itemId = tip.itemId,
            authorId = tip.authorId,
            authorName = authorName,
            message = tip.message,
            createdAt = tip.createdAt,
            parentTipId = tip.parentTipId,
            isReply = tip.parentTipId != null
        )
    }
}