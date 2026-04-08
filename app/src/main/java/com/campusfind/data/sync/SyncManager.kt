package com.campusfind.data.sync

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SyncManager.kt
 *
 * Schedules and manages the background sync WorkManager task.
 *
 * Called from:
 * - MainActivity.onCreate() — schedules periodic sync on app start
 * - Can be called manually to trigger an immediate sync
 *
 * Constraints:
 * - WiFi only (UNMETERED) — avoids mobile data charges
 * - Battery not low — respects device battery state
 * - 15-minute interval — minimum WorkManager periodic interval
 *
 * See: DEC-008 (WorkManager), TASKS.md TASK-205
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)  // any network
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval         = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,   // don't replace if already scheduled
            syncRequest
        )
    }

    // Trigger an immediate one-time sync (e.g. on app foreground)
    fun syncNow() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueue(syncRequest)
    }

    fun cancelSync() {
        WorkManager.getInstance(context).cancelUniqueWork(SyncWorker.WORK_NAME)
    }
}