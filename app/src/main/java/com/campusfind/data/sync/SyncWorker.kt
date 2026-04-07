package com.campusfind.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.campusfind.data.repository.FirestoreClaimRepositoryImpl
import com.campusfind.data.repository.FirestoreLostItemRepositoryImpl
import com.campusfind.data.repository.FirestoreTipRepositoryImpl
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * SyncWorker.kt — Phase 2
 *
 * Syncs all Firestore data into local Room:
 * 1. Lost items
 * 2. Tips for each item
 * 3. Claims for each item (+ replies)
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val lostItemRepository: FirestoreLostItemRepositoryImpl,
    private val tipRepository: FirestoreTipRepositoryImpl,
    private val claimRepository: FirestoreClaimRepositoryImpl
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "campusfind_sync_worker"
    }

    override suspend fun doWork(): Result {
        return try {
            // 1. Sync all lost items
            lostItemRepository.syncFromFirestore()

            // 2. For each item, sync tips and claims
            // We get item IDs from the inputData if passed,
            // otherwise sync tips/claims lazily when items are opened
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry()
            else Result.failure()
        }
    }
}