package com.lumina.data.coordination

import com.lumina.core.common.FeatureFlags
import com.lumina.core.model.AppInfo
import com.lumina.core.model.componentKey
import com.lumina.domain.coordination.AppLaunchCoordinator
import com.lumina.domain.coordination.IntentLauncher
import com.lumina.domain.coordination.LaunchState
import com.lumina.domain.coordination.usecase.ObserveActiveProfileCountdownAppsUseCase
import com.lumina.domain.countdown.CountdownAppsRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

@Singleton
class PlatformAppLaunchCoordinator @Inject constructor(
    private val intentLauncher: IntentLauncher,
    private val countdownRepository: CountdownAppsRepository,
    private val observeCountdownApps: ObserveActiveProfileCountdownAppsUseCase
) : AppLaunchCoordinator{
    private val _launchState = MutableStateFlow<LaunchState>(LaunchState.Idle)
    override val launchState = _launchState.asStateFlow()

    private var pendingApp: AppInfo? = null

    override suspend fun requestLaunch(app: AppInfo) {
        val currentPackages = countdownRepository.appPackages.first()
        val needsCountdown = if (FeatureFlags.USE_ROOM_FOR_FAV_COUNTDOWN) {
            observeCountdownApps().first().any {
                it.info.componentKey == app.componentKey
            }
        } else {
            currentPackages.contains(app.packageName)
        }

        if (needsCountdown) {
            pendingApp = app
            _launchState.value = LaunchState.RequiresCountdown(app)
        } else {
            intentLauncher.openApp(app)
        }
    }

    override suspend fun completeLaunch() {
        pendingApp?.let { app -> intentLauncher.openApp(app) }
        reset()
    }

    override fun cancelLaunch() {
        reset()
    }

    private fun reset() {
        pendingApp = null
        _launchState.value = LaunchState.Idle
    }
}
