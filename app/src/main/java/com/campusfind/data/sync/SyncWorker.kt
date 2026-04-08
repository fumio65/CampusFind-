package com.campusfind.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.campusfind.data.local.database.ClaimDao
import com.campusfind.data.local.database.ClaimEntity
import com.campusfind.data.local.database.ClaimReplyDao
import com.campusfind.data.local.database.ClaimReplyEntity
import com.campusfind.data.local.database.LostItemDao
import com.campusfind.data.local.database.LostItemEntity
import com.campusfind.data.local.database.TipDao
import com.campusfind.data.local.database.TipEntity
import com.campusfind.data.local.database.UserDao
import com.campusfind.data.local.database.UserEntity
import com.campusfind.data.remote.dto.ClaimDto
import com.campusfind.data.remote.dto.ClaimReplyDto
import com.campusfind.data.remote.dto.LostItemDto
import com.campusfind.data.remote.dto.TipDto
import com.campusfind.data.remote.dto.UserDto
import com.campusfind.data.remote.source.ClaimRemoteDataSource
import com.campusfind.data.remote.source.ClaimReplyRemoteDataSource
import com.campusfind.data.remote.source.LostItemRemoteDataSource
import com.campusfind.data.remote.source.TipRemoteDataSource
import com.campusfind.data.remote.source.UserRemoteDataSource
import com.campusfind.domain.model.SyncStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val userDao: UserDao,
    private val lostItemDao: LostItemDao,
    private val claimDao: ClaimDao,
    private val claimReplyDao: ClaimReplyDao,
    private val tipDao: TipDao,
    private val userRemote: UserRemoteDataSource,
    private val lostItemRemote: LostItemRemoteDataSource,
    private val claimRemote: ClaimRemoteDataSource,
    private val claimReplyRemote: ClaimReplyRemoteDataSource,
    private val tipRemote: TipRemoteDataSource,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            pushPending()
            pullAll()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    // ── Push local PENDING_SYNC records to Supabase ──────────────────────

    private suspend fun pushPending() {
        pushUsers()
        pushLostItems()
        pushClaims()
        pushClaimReplies()
        pushTips()
    }

    private suspend fun pushUsers() {
        userDao.getPendingSyncUsers().forEach { entity ->
            try {
                userRemote.upsert(entity.toDto())
                userDao.updateSyncStatus(entity.id, SyncStatus.SYNCED.name)
            } catch (e: Exception) {
                userDao.updateSyncStatus(entity.id, SyncStatus.SYNC_FAILED.name)
            }
        }
    }

    private suspend fun pushLostItems() {
        lostItemDao.getPendingSyncItems().forEach { entity ->
            try {
                lostItemRemote.upsert(entity.toDto())
                lostItemDao.updateSyncStatus(entity.id, SyncStatus.SYNCED.name)
            } catch (e: Exception) {
                lostItemDao.updateSyncStatus(entity.id, SyncStatus.SYNC_FAILED.name)
            }
        }
    }

    private suspend fun pushClaims() {
        claimDao.getPendingSyncClaims().forEach { entity ->
            try {
                claimRemote.upsert(entity.toDto())
                claimDao.updateSyncStatus(entity.id, SyncStatus.SYNCED.name)
            } catch (e: Exception) {
                claimDao.updateSyncStatus(entity.id, SyncStatus.SYNC_FAILED.name)
            }
        }
    }

    private suspend fun pushClaimReplies() {
        claimReplyDao.getPendingSyncReplies().forEach { entity ->
            try {
                claimReplyRemote.upsert(entity.toDto())
                claimReplyDao.updateSyncStatus(entity.id, SyncStatus.SYNCED.name)
            } catch (e: Exception) {
                claimReplyDao.updateSyncStatus(entity.id, SyncStatus.SYNC_FAILED.name)
            }
        }
    }

    private suspend fun pushTips() {
        tipDao.getPendingSyncTips().forEach { entity ->
            try {
                tipRemote.upsert(entity.toDto())
                tipDao.updateSyncStatus(entity.id, SyncStatus.SYNCED.name)
            } catch (e: Exception) {
                tipDao.updateSyncStatus(entity.id, SyncStatus.SYNC_FAILED.name)
            }
        }
    }

    // ── Pull all records from Supabase and merge into Room ───────────────
    // Order: users first (FK dependency), then items, claims, replies, tips

    private suspend fun pullAll() {
        pullUsers()
        pullLostItems()
        pullClaims()
        pullClaimReplies()
        pullTips()
    }

    private suspend fun pullUsers() {
        userRemote.fetchAll().forEach { dto ->
            // Step 1: Insert only if this user doesn't exist locally yet.
            //         INSERT OR IGNORE guarantees the local password_hash is never touched.
            userDao.insertFromRemoteIfAbsent(dto.id, dto.fullName, dto.email, dto.messengerHandle, dto.createdAt)
            // Step 2: Update non-sensitive profile fields for users that already exist locally.
            userDao.updateNonSensitiveFromRemote(dto.id, dto.fullName, dto.messengerHandle)
        }
    }

    private suspend fun pullLostItems() {
        val items = lostItemRemote.fetchAll().map { it.toEntity() }
        lostItemDao.upsertAll(items)
    }

    private suspend fun pullClaims() {
        val claims = claimRemote.fetchAll().map { it.toEntity() }
        claimDao.upsertAll(claims)
    }

    private suspend fun pullClaimReplies() {
        val replies = claimReplyRemote.fetchAll().map { it.toEntity() }
        claimReplyDao.upsertAll(replies)
    }

    private suspend fun pullTips() {
        val tips = tipRemote.fetchAll().map { it.toEntity() }
        tipDao.upsertAll(tips)
    }

    // ── DTO converters ───────────────────────────────────────────────────

    private fun UserEntity.toDto() = UserDto(
        id = id,
        fullName = fullName,
        email = email,
        messengerHandle = messengerHandle,
        createdAt = createdAt
    )

    private fun LostItemEntity.toDto() = LostItemDto(
        id = id,
        title = title,
        description = description,
        location = location,
        status = status,
        reportedBy = reportedBy,
        reportedAt = reportedAt,
        lastModifiedAt = lastModifiedAt,
        photoUri = photoUri
    )

    private fun ClaimEntity.toDto() = ClaimDto(
        id = id,
        itemId = itemId,
        claimedBy = claimedBy,
        message = message,
        photoUri = photoUri,
        status = status,
        claimedAt = claimedAt,
        reviewedAt = reviewedAt
    )

    private fun ClaimReplyEntity.toDto() = ClaimReplyDto(
        id = id,
        claimId = claimId,
        authorId = authorId,
        message = message,
        createdAt = createdAt
    )

    private fun TipEntity.toDto() = TipDto(
        id = id,
        itemId = itemId,
        authorId = authorId,
        message = message,
        createdAt = createdAt,
        parentTipId = parentTipId
    )

    private fun LostItemDto.toEntity() = LostItemEntity(
        id = id,
        title = title,
        description = description,
        location = location,
        status = status,
        reportedBy = reportedBy,
        reportedAt = reportedAt,
        lastModifiedAt = lastModifiedAt,
        photoUri = photoUri,
        syncStatus = SyncStatus.SYNCED.name
    )

    private fun ClaimDto.toEntity() = ClaimEntity(
        id = id,
        itemId = itemId,
        claimedBy = claimedBy,
        message = message,
        photoUri = photoUri,
        status = status,
        claimedAt = claimedAt,
        reviewedAt = reviewedAt,
        syncStatus = SyncStatus.SYNCED.name
    )

    private fun ClaimReplyDto.toEntity() = ClaimReplyEntity(
        id = id,
        claimId = claimId,
        authorId = authorId,
        message = message,
        createdAt = createdAt,
        syncStatus = SyncStatus.SYNCED.name
    )

    private fun TipDto.toEntity() = TipEntity(
        id = id,
        itemId = itemId,
        authorId = authorId,
        message = message,
        createdAt = createdAt,
        parentTipId = parentTipId,
        syncStatus = SyncStatus.SYNCED.name
    )
}
