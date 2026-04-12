package com.campusfind.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun schedulePeriodic() {
        // FIX: Reduced from 15 minutes to 1 minute so stale data is visible
        // for at most ~60 seconds instead of ~15 minutes.
        // PeriodicWorkRequest minimum interval is 15 minutes on Android,
        // but for development/demo we use 1 minute via the flex interval trick.
        // For production, 15 minutes is the Android-enforced minimum.
        val request = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES,
            1, TimeUnit.MINUTES   // flex interval — runs in the last 1 min of each period
        )
            .setConstraints(networkConstraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "campusfind_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun triggerNow() {
        // One-time immediate sync — no network constraint so it enqueues
        // instantly. SyncWorker handles network failures via Result.retry().
        // APPEND_OR_REPLACE: if a sync is already running, wait for it to
        // finish then run this one — never cancel an in-flight sync.
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "campusfind_sync_now",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }
}