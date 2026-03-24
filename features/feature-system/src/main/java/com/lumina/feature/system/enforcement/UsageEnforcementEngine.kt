package com.lumina.feature.system.enforcement

import com.lumina.core.logging.Logger
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.usage.UsageRepository
import com.lumina.domain.usage.model.UsageTimeRange
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val CHECK_USAGE_LIMIT_MILLISECONDS = 30_000L

@Singleton
class UsageEnforcementEngine @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val usageRepository: UsageRepository,
    private val logger: Logger
) {
    private val TAG = this::class.java.simpleName

    private var monitorJob: Job? = null

    fun startMonitoring(
        scope: CoroutineScope,
        packageName: String,
        userHandleNumber: Long,
        profileId: String,
        onLimitReached: () -> Unit
    ) {
        monitorJob?.cancel()

        monitorJob = scope.launch {
            val limitMinutes = profileRepository.getRecommendedUsageMinutes(profileId, packageName, userHandleNumber)
                ?: return@launch

            val limitMs = limitMinutes * 60_000L
            if (limitMs <= 0) return@launch

            while (isActive) {
                val stats = usageRepository.getSessionStats(
                    packageName, userHandleNumber, profileId, UsageTimeRange.TODAY
                )

                if (stats != null && stats.totalMs >= limitMs) {
                    logger.d(TAG, "Usage Limit reached for $packageName.")
                    onLimitReached()
                    break
                }

                delay(CHECK_USAGE_LIMIT_MILLISECONDS)
            }
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
    }
}
