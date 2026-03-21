package com.lumina.data.usage

import com.lumina.core.android.di.ApplicationScope
import com.lumina.core.database.dao.AppUsageDao
import com.lumina.core.database.dao.ProfileDao
import com.lumina.core.database.entity.AppUsageSessionEntity
import com.lumina.core.database.entity.ProfileSwitchLogEntity
import com.lumina.core.logging.Logger
import com.lumina.data.usage.UsageConstants.HEARTBEAT_INTERVAL_MILLISECONDS
import com.lumina.data.usage.UsageConstants.MIN_SESSION_MILLISECONDS
import com.lumina.domain.usage.AppUsageTracker
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Singleton
class DefaultAppUsageTracker @Inject constructor(
    @param:ApplicationScope private val scope: CoroutineScope,
    private val appUsageDao: AppUsageDao,
    private val logger: Logger
) : AppUsageTracker {
    private val TAG = this::class.java.simpleName

    private val activeSessions = ConcurrentHashMap<String, ActiveSession>()

    @OptIn(ExperimentalUuidApi::class)
    override fun onAppForegrounded(
        packageName: String,
        userHandleNumber: Long,
        profileId: String,
        timestamp: Long
    ) {
        val key = sessionKey(packageName, userHandleNumber)
        activeSessions[key]?.let { closeSession(it, timestamp) }

        val sessionId = Uuid.random().toString()
        val session = ActiveSession(
            sessionId = sessionId,
            packageName = packageName,
            userHandleNumber = userHandleNumber,
            profileId = profileId,
            startTime = timestamp
        )
        session.heartbeatJob = launchHeartbeat(session)
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

    private fun launchHeartbeat(session: ActiveSession): Job {
        return scope.launch {
            delay(MIN_SESSION_MILLISECONDS)

            val key = sessionKey(session.packageName, session.userHandleNumber)
            if (activeSessions[key]?.sessionId != session.sessionId) return@launch

            try {
                appUsageDao.insertSession(
                    AppUsageSessionEntity(
                        sessionId = session.sessionId,
                        profileId = session.profileId,
                        packageName = session.packageName,
                        userHandleNumber = session.userHandleNumber,
                        startTime = session.startTime,
                        endTime = System.currentTimeMillis()
                    )
                )
                activeSessions[key]?.writtenToDb = true
            } catch (e: Exception) {
                logger.e(TAG, "Failed initial write for ${session.packageName}.", e)
                return@launch
            }

            while (isActive) {
                delay(HEARTBEAT_INTERVAL_MILLISECONDS)
                if (activeSessions[key]?.sessionId != session.sessionId) break

                try {
                    appUsageDao.updateSessionEndTime(session.sessionId, System.currentTimeMillis())
                } catch (e: Exception) {
                    logger.e(TAG, "Heartbeat write failed for ${session.packageName}.", e)
                }
            }
        }
    }

    private fun sessionKey(packageName: String, userHandleNumber: Long) = "$packageName:$userHandleNumber"

    private fun closeSession(session: ActiveSession, endTime: Long) {
        session.heartbeatJob?.cancel()

        val key = sessionKey(session.packageName, session.userHandleNumber)
        activeSessions.remove(key)

        val duration = endTime - session.startTime
        if (duration < MIN_SESSION_MILLISECONDS || !session.writtenToDb) return

        scope.launch {
            appUsageDao.updateSessionEndTime(session.sessionId, endTime)
        }
    }
}
