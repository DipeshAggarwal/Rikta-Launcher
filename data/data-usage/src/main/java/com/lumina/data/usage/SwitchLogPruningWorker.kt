package com.lumina.data.usage

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lumina.core.database.dao.AppUsageDao
import com.lumina.core.logging.Logger
import com.lumina.data.usage.UsageConstants.SWITCH_LOG_RETENTION_DAYS
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SwitchLogPruningWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val appUsageDao: AppUsageDao,
    private val logger: Logger
) : CoroutineWorker(context, params) {
    private val TAG = this::class.java.simpleName

    override suspend fun doWork(): Result {
        return try {
            val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(SWITCH_LOG_RETENTION_DAYS)

            appUsageDao.pruneOldSwitchLog(cutoff)
            logger.d(TAG, "Pruned switch log entries older than $SWITCH_LOG_RETENTION_DAYS days.")
            Result.success()
        } catch (e: Exception) {
            logger.e(TAG, "Switch log pruning failed.", e)
            Result.retry()
        }
    }
}
