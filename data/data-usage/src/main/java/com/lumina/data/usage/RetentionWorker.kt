package com.lumina.data.usage

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lumina.core.database.dao.AppUsageDao
import com.lumina.core.logging.Logger
import com.lumina.domain.usage.UsageSettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class RetentionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val appUsageDao: AppUsageDao,
    private val usageSettingsRepository: UsageSettingsRepository,
    private val logger: Logger
) : CoroutineWorker(context, params) {
    private val TAG = this::class.java.simpleName

    override suspend fun doWork(): Result {
        return try {
            val retentionDays = usageSettingsRepository.rawRetentionDays.first()
            val cutOff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(retentionDays.toLong())

            appUsageDao.deleteSessionsOlderThan(cutOff)
            logger.d(TAG, "Deleted sessions older than $retentionDays days.")
            Result.success()
        } catch (e: Exception) {
            logger.e(TAG, "Retention pruning failed", e)
            Result.retry()
        }
    }
}
