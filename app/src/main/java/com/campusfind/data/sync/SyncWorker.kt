package com.campusfind.data.sync

import android.content.Context
import android.util.Log
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
import com.campusfind.data.remote.source.PhotoRemoteDataSource
import com.campusfind.data.remote.source.TipRemoteDataSource
import com.campusfind.data.remote.source.UserRemoteDataSource
import com.campusfind.domain.model.SyncStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

private const val TAG = "SYNC_DEBUG"

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
    private val photoRemote: PhotoRemoteDataSource,
) : CoroutineWorker(context, params) {

    // Local to each doWork() call — cannot leak between WorkManager runs
    private var pushedItemIds = mutableSetOf<String>()

    override suspend fun doWork(): Result {
        Log.d(TAG, "=== SyncWorker START attempt=$runAttemptCount ===")
        return try {
            pushedItemIds = mutableSetOf() // fresh set every run — never leaks between WorkManager executions
            pushPending()
            pullAll()
            Log.d(TAG, "=== SyncWorker SUCCESS ===")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "=== SyncWorker FAILED: ${e::class.simpleName}: ${e.message} ===")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    // ── Push ─────────────────────────────────────────────────────────────

    private suspend fun pushPending() {
        pushUsers()
        pushLostItems()
        coroutineScope {
            val c = async { pushClaims() }
            val r = async { pushClaimReplies() }
            val t = async { pushTips() }
            awaitAll(c, r, t)
        }
    }

    private suspend fun pushUsers() {
        val pending = userDao.getPendingSyncUsers()
        Log.d(TAG, "pushUsers: ${pending.size} pending")
        // Debug: log the sync_status of ALL users to confirm PENDING_SYNC is being set
        pending.forEach { u ->
            Log.d(TAG, "pushUsers pending: email=${u.email} syncStatus=${u.syncStatus} photo=${u.profilePhotoUri?.takeLast(20)}")
        }
        pending.forEach { entity ->
            try {
                Log.d(TAG, "pushUsers: entity=${entity.email} photoUri=${entity.profilePhotoUri}")
                // Upload profile photo if it's a local file (not already a remote URL)
                val remotePhotoUrl: String? = when {
                    entity.profilePhotoUri == null -> {
                        Log.d(TAG, "pushUsers: no photo"); null
                    }
                    photoRemote.isRemoteUrl(entity.profilePhotoUri) -> {
                        Log.d(TAG, "pushUsers: already remote url"); entity.profilePhotoUri
                    }
                    else -> {
                        Log.d(TAG, "pushUsers: uploading local photo ${entity.profilePhotoUri}")
                        val url = photoRemote.uploadProfilePhoto(entity.profilePhotoUri, entity.id)
                        Log.d(TAG, "pushUsers: upload result=$url")
                        url
                    }
                }
                // Write remote URL back to Room if photo was just uploaded
                if (remotePhotoUrl != null && remotePhotoUrl != entity.profilePhotoUri) {
                    userDao.updateProfilePhoto(entity.id, remotePhotoUrl)
                    Log.d(TAG, "pushUsers: wrote remote url back to Room")
                }
                userRemote.upsert(entity.toDto().copy(profilePhotoUri = remotePhotoUrl))
                userDao.updateSyncStatus(entity.id, SyncStatus.SYNCED.name)
                Log.d(TAG, "pushUsers: synced '${entity.email}'")
            } catch (e: Exception) {
                Log.e(TAG, "pushUsers FAILED ${entity.email}: ${e.message}", e)
                userDao.updateSyncStatus(entity.id, SyncStatus.SYNC_FAILED.name)
            }
        }
    }

    private suspend fun pushLostItems() {
        val pending = lostItemDao.getPendingSyncItems()
        Log.d(TAG, "pushLostItems: ${pending.size} pending")
        pending.forEach { entity ->
            try {
                val remotePhotoUrl: String? = when {
                    entity.photoUri == null                   -> null
                    photoRemote.isRemoteUrl(entity.photoUri) -> entity.photoUri
                    else -> photoRemote.uploadItemPhoto(entity.photoUri, entity.id)
                }
                lostItemRemote.upsert(entity.toDto().copy(photoUri = remotePhotoUrl))
                if (remotePhotoUrl != null && remotePhotoUrl != entity.photoUri) {
                    lostItemDao.updatePhotoUri(entity.id, remotePhotoUrl)
                    photoRemote.deleteOldItemPhotos(itemId = entity.id, keepUrl = remotePhotoUrl)
                }
                lostItemDao.updateSyncStatus(entity.id, SyncStatus.SYNCED.name)
                pushedItemIds.add(entity.id)
                Log.d(TAG, "pushLostItems: synced '${entity.title}'")
            } catch (e: Exception) {
                Log.e(TAG, "pushLostItems FAILED '${entity.title}': ${e.message}")
                lostItemDao.updateSyncStatus(entity.id, SyncStatus.SYNC_FAILED.name)
            }
        }
    }

    private suspend fun pushClaims() {
        val pending = claimDao.getPendingSyncClaims()
        Log.d(TAG, "pushClaims: ${pending.size} pending")
        pending.forEach { entity ->
            try {
                claimRemote.upsert(entity.toDto())
                claimDao.updateSyncStatus(entity.id, SyncStatus.SYNCED.name)
            } catch (e: Exception) {
                Log.e(TAG, "pushClaims FAILED: ${e.message}")
                claimDao.updateSyncStatus(entity.id, SyncStatus.SYNC_FAILED.name)
            }
        }
    }

    private suspend fun pushClaimReplies() {
        val pending = claimReplyDao.getPendingSyncReplies()
        Log.d(TAG, "pushClaimReplies: ${pending.size} pending")
        pending.forEach { entity ->
            try {
                claimReplyRemote.upsert(entity.toDto())
                claimReplyDao.updateSyncStatus(entity.id, SyncStatus.SYNCED.name)
            } catch (e: Exception) {
                Log.e(TAG, "pushClaimReplies FAILED: ${e.message}")
                claimReplyDao.updateSyncStatus(entity.id, SyncStatus.SYNC_FAILED.name)
            }
        }
    }

    private suspend fun pushTips() {
        val pending = tipDao.getPendingSyncTips()
        Log.d(TAG, "pushTips: ${pending.size} pending")
        pending.forEach { entity ->
            try {
                tipRemote.upsert(entity.toDto())
                tipDao.updateSyncStatus(entity.id, SyncStatus.SYNCED.name)
            } catch (e: Exception) {
                Log.e(TAG, "pushTips FAILED: ${e.message}")
                tipDao.updateSyncStatus(entity.id, SyncStatus.SYNC_FAILED.name)
            }
        }
    }

    // ── Pull ─────────────────────────────────────────────────────────────

    private suspend fun pullAll() = coroutineScope {
        val usersDeferred   = async { userRemote.fetchAll() }
        val itemsDeferred   = async { lostItemRemote.fetchAll() }
        val claimsDeferred  = async { claimRemote.fetchAll() }
        val repliesDeferred = async { claimReplyRemote.fetchAll() }
        val tipsDeferred    = async { tipRemote.fetchAll() }

        val users   = usersDeferred.await()
        val items   = itemsDeferred.await()
        val claims  = claimsDeferred.await()
        val replies = repliesDeferred.await()
        val tips    = tipsDeferred.await()

        Log.d(TAG, "pullAll fetched: users=${users.size} items=${items.size} " +
                "claims=${claims.size} replies=${replies.size} tips=${tips.size}")

        users.forEach { dto ->
            userDao.insertFromRemoteIfAbsent(
                dto.id, dto.fullName, dto.email,
                dto.passwordHash, dto.messengerHandle, dto.createdAt,
                dto.profilePhotoUri
            )
            // Update name/messenger — only for rows that are already SYNCED.
            // PENDING_SYNC rows have local edits that haven't been pushed yet.
            userDao.updateNonSensitiveFromRemote(dto.id, dto.fullName, dto.messengerHandle)
            // Only update photo if remote actually has a URL — never overwrite
            // a local pending photo path with null from Supabase.
            if (!dto.profilePhotoUri.isNullOrBlank()) {
                userDao.updateRemotePhotoUri(dto.id, dto.profilePhotoUri)
            }
        }

        items.forEach { dto ->
            if (dto.id in pushedItemIds) {
                Log.d(TAG, "pullAll: skipping pushed item '${dto.title}'")
                return@forEach
            }

            // Log what Supabase is sending
            Log.d(TAG, "SUPABASE item: id=${dto.id.takeLast(8)} title='${dto.title}' status=${dto.status}")

            lostItemDao.upsertFromRemote(
                id             = dto.id,
                title          = dto.title,
                description    = dto.description,
                location       = dto.location,
                status         = dto.status,
                reportedBy     = dto.reportedBy,
                reportedAt     = dto.reportedAt,
                lastModifiedAt = dto.lastModifiedAt,
                remotePhotoUri = dto.photoUri?.takeIf { it.startsWith("https://") }
            )

            // Log what Room actually has after the upsert
            val after = lostItemDao.getItemById(dto.id)
            Log.d(TAG, "ROOM after upsert: id=${dto.id.takeLast(8)} title='${after?.title}' status=${after?.status}")
        }
        Log.d(TAG, "pullAll: items written to Room — Flow should emit now")

        coroutineScope {
            val wc = async { claimDao.upsertAll(claims.map { it.toEntity() }) }
            val wr = async { claimReplyDao.upsertAll(replies.map { it.toEntity() }) }
            val wt = async { tipDao.upsertAll(tips.map { it.toEntity() }) }
            awaitAll(wc, wr, wt)
        }

        Log.d(TAG, "pullAll: DONE")
    }

    // ── DTO / Entity converters ──────────────────────────────────────────

    private fun UserEntity.toDto() = UserDto(
        id = id, fullName = fullName, email = email,
        messengerHandle = messengerHandle, createdAt = createdAt, passwordHash = passwordHash,
        profilePhotoUri = profilePhotoUri
    )

    private fun LostItemEntity.toDto() = LostItemDto(
        id = id, title = title, description = description, location = location,
        status = status, reportedBy = reportedBy, reportedAt = reportedAt,
        lastModifiedAt = lastModifiedAt, photoUri = photoUri
    )

    private fun ClaimEntity.toDto() = ClaimDto(
        id = id, itemId = itemId, claimedBy = claimedBy, message = message,
        photoUri = photoUri, status = status, claimedAt = claimedAt, reviewedAt = reviewedAt
    )

    private fun ClaimReplyEntity.toDto() = ClaimReplyDto(
        id = id, claimId = claimId, authorId = authorId,
        message = message, createdAt = createdAt
    )

    private fun TipEntity.toDto() = TipDto(
        id = id, itemId = itemId, authorId = authorId,
        message = message, createdAt = createdAt, parentTipId = parentTipId
    )

    private fun LostItemDto.toEntity() = LostItemEntity(
        id = id, title = title, description = description, location = location,
        status = status, reportedBy = reportedBy, reportedAt = reportedAt,
        lastModifiedAt = lastModifiedAt, photoUri = photoUri,
        syncStatus = SyncStatus.SYNCED.name
    )

    private fun ClaimDto.toEntity() = ClaimEntity(
        id = id, itemId = itemId, claimedBy = claimedBy, message = message,
        photoUri = photoUri, status = status, claimedAt = claimedAt,
        reviewedAt = reviewedAt, syncStatus = SyncStatus.SYNCED.name
    )

    private fun ClaimReplyDto.toEntity() = ClaimReplyEntity(
        id = id, claimId = claimId, authorId = authorId, message = message,
        createdAt = createdAt, syncStatus = SyncStatus.SYNCED.name
    )

    private fun TipDto.toEntity() = TipEntity(
        id = id, itemId = itemId, authorId = authorId, message = message,
        createdAt = createdAt, parentTipId = parentTipId,
        syncStatus = SyncStatus.SYNCED.name
    )
}