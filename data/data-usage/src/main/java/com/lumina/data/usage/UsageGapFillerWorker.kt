package com.lumina.data.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process.myUserHandle
import android.os.UserManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lumina.core.common.time.TimeProvider
import com.lumina.core.database.dao.AppUsageDao
import com.lumina.core.database.entity.AppUsageSessionEntity
import com.lumina.core.logging.Logger
import com.lumina.core.model.SystemProfileIds
import com.lumina.data.usage.UsageConstants.MIN_SESSION_MILLISECONDS
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.UUID
import java.util.concurrent.TimeUnit

@HiltWorker
class UsageGapFillerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val usageStatsManager: UsageStatsManager,
    private val appUsageDao: AppUsageDao,
    private val timeProvider: TimeProvider,
    private val userManager: UserManager,
    private val logger: Logger
) : CoroutineWorker(context, params) {
    private val TAG = this::class.java.simpleName

    override suspend fun doWork(): Result {
        return try {
            fillGaps()
            Result.success()
        } catch (e: SecurityException) {
            logger.w(TAG, "PACKAGE_USAGE_STATS not granted. Skipping")
            Result.success()
        } catch (e: Exception) {
            logger.e(TAG, "Gap filling failed.", e)
            Result.retry()
        }
    }

    private suspend fun fillGaps() {
        val now = timeProvider.now()
        val since = now - TimeUnit.HOURS.toMillis(1)

        val userHandleNumber = userManager.getSerialNumberForUser(myUserHandle())

        val events = usageStatsManager.queryEvents(since, now)
        val event = UsageEvents.Event()
        val resumeMap = mutableMapOf<String, Long>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    resumeMap[event.packageName] = event.timeStamp
                }

                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    val resumeTime = resumeMap.remove(event.packageName) ?: continue
                    val duration = event.timeStamp - resumeTime
                    if (duration < MIN_SESSION_MILLISECONDS) continue

                    val hasRecord = appUsageDao.hasOverlappingSession(
                        event.packageName,
                        userHandleNumber,
                        resumeTime,
                        event.timeStamp
                    ) > 0

                    if (!hasRecord) {
                        val switchLog = appUsageDao.getActiveProfileAtTime(resumeTime)
                        val profileId = switchLog?.profileId ?: SystemProfileIds.DEFAULT

                        appUsageDao.insertSession(
                            AppUsageSessionEntity(
                                sessionId = UUID.randomUUID().toString(),
                                profileId = profileId,
                                packageName = event.packageName,
                                userHandleNumber = userHandleNumber,
                                startTime = resumeTime,
                                endTime = event.timeStamp
                            )
                        )
                        logger.d(TAG, "Gap filled: ${event.packageName} for $profileId")
                    }
                }
            }
        }
    }

}
