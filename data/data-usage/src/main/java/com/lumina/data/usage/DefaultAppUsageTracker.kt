package com.lumina.data.usage

import com.lumina.core.android.di.ApplicationScope
import com.lumina.core.common.time.TimeProvider
import com.lumina.core.database.dao.AppUsageDao
import com.lumina.core.database.entity.AppUsageSessionEntity
import com.lumina.core.database.entity.ProfileSwitchLogEntity
import com.lumina.core.logging.Logger
import com.lumina.data.usage.UsageConstants.MIN_SESSION_MILLISECONDS
import com.lumina.domain.usage.AppUsageTracker
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Singleton
class DefaultAppUsageTracker @Inject constructor(
    @param:ApplicationScope private val scope: CoroutineScope,
    private val appUsageDao: AppUsageDao,
    private val timeProvider: TimeProvider,
    private val heartbeatTicker: HeartbeatTicker,
    private val logger: Logger
) : AppUsageTracker {
    private val TAG = this::class.java.simpleName

    private val activeSessions = ConcurrentHashMap<String, ActiveSession>()

    init {
        startHeartbeatLoop()
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun onAppForegrounded(
        packageName: String,
        userHandleNumber: Long,
        profileId: String,
        timestamp: Long
    ) {
        val key = sessionKey(packageName, userHandleNumber)

        // Close all other active sessions — PiP and split screen  not yet supported
        activeSessions.values
            .filter { sessionKey(it.packageName, it.userHandleNumber) != key }
            .forEach { closeSession(it, timestamp) }

        // Close same session, if it exists.
        activeSessions[key]?.let { closeSession(it, timestamp) }

        val sessionId = Uuid.random().toString()
        val session = ActiveSession(
            sessionId = sessionId,
            packageName = packageName,
            userHandleNumber = userHandleNumber,
            profileId = profileId,
            startTime = timestamp
        )
        activeSessions[key] = session
    }

    override fun onAppBackgrounded(
        packageName: String,
        userHandleNumber: Long,
        timestamp: Long
    ) {
        val key = sessionKey(packageName, userHandleNumber)
        val session = activeSessions.remove(key) ?: return
        closeSession(session, timestamp)
    }

    override fun onProfileSwitched(newProfileId: String, timestamp: Long) {
        activeSessions.values.toList().forEach { closeSession(it, timestamp) }
        activeSessions.clear()

        scope.launch {
            try {
                appUsageDao.insertSwitchLog(
                    ProfileSwitchLogEntity(
                        profileId = newProfileId,
                        switchedAt = timestamp
                    )
                )
            } catch (e: Exception) {
                logger.e(TAG, "Failed to log profile switch.", e)
            }
        }
    }

    override fun onBootCompleted(rebootTime: Long) {
        scope.launch {
            appUsageDao.closeOrphanedSessions(rebootTime)
            logger.d(TAG, "Closed orphaned sessions from last boot.")
        }
    }

    private fun startHeartbeatLoop() {
        scope.launch {
            while (isActive) {
                heartbeatTicker.awaitNextTick()
                processHeartbeat()
            }
        }
    }

    private suspend fun processHeartbeat() {
        val now = timeProvider.now()
        activeSessions.values.forEach { session ->
            val duration = now - session.startTime
            if (duration < MIN_SESSION_MILLISECONDS) return@forEach

            if (!session.writtenToDb) {
                try {
                    appUsageDao.insertSession(
                        AppUsageSessionEntity(
                            sessionId = session.sessionId,
                            profileId = session.profileId,
                            packageName = session.packageName,
                            userHandleNumber = session.userHandleNumber,
                            startTime = session.startTime,
                            endTime = now
                        )
                    )
                    session.writtenToDb = true
                    logger.d(TAG, "Initial write for ${session.packageName}.")
                } catch (e: Exception) {
                    logger.e(TAG, "Failed initial write for ${session.packageName}.", e)
                    return@forEach
                }
            } else {
                try {
                    appUsageDao.updateSessionEndTime(session.sessionId, now)
                    logger.d(TAG, "Heartbeat write done for ${session.packageName}.")
                } catch (e: Exception) {
                    logger.e(TAG, "Heartbeat write failed for ${session.packageName}.", e)
                }
            }
        }
    }

    private fun closeSession(session: ActiveSession, endTime: Long) {
        val key =  sessionKey(session.packageName, session.userHandleNumber)
        activeSessions.remove(key)

        val duration = endTime - session.startTime
        if (duration < MIN_SESSION_MILLISECONDS || !session.writtenToDb) return

        scope.launch {
            try {
                appUsageDao.updateSessionEndTime(session.sessionId, endTime)
            } catch (e: Exception) {
                logger.e(TAG, "Failed to close session ${session.packageName}.", e)
            }
        }
    }

    private fun sessionKey(packageName: String, userHandleNumber: Long) = "$packageName:$userHandleNumber"
}
