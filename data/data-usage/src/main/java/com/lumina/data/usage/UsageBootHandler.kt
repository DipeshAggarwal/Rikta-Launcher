package com.lumina.data.usage

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lumina.core.common.BootHandler
import com.lumina.domain.usage.AppUsageTracker
import jakarta.inject.Inject
import java.util.concurrent.TimeUnit

class UsageBootHandler @Inject constructor(
    private val appUsageTracker: AppUsageTracker,
    private val workManager: WorkManager
) : BootHandler {

    override fun onBoot(rebootTime: Long) {
        appUsageTracker.onBootCompleted(rebootTime)
        scheduleWorkers()
    }

    private fun scheduleWorkers() {
        workManager.enqueueUniquePeriodicWork(
            "usage_retention",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<RetentionWorker>(1, TimeUnit.DAYS).build()
        )

        workManager.enqueueUniquePeriodicWork(
            "usage_gap_filler",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<UsageGapFillerWorker>(1, TimeUnit.HOURS).build()
        )

        workManager.enqueueUniquePeriodicWork(
            "switch_log_pruning",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<SwitchLogPruningWorker>(1, TimeUnit.DAYS).build()
        )
    }
}
