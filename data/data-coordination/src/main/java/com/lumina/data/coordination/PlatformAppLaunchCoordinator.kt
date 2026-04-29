package com.lumina.data.coordination

import com.lumina.core.common.FeatureFlags
import com.lumina.core.model.LauncherItem
import com.lumina.domain.coordination.AppLaunchCoordinator
import com.lumina.domain.coordination.IntentLauncher
import com.lumina.domain.coordination.LaunchState
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
) : AppLaunchCoordinator{
    private val _launchState = MutableStateFlow<LaunchState>(LaunchState.Idle)
    override val launchState = _launchState.asStateFlow()

    private var pendingApp: LauncherItem.App? = null

    override suspend fun requestLaunch(app: LauncherItem.App) {
        if (FeatureFlags.USE_ROOM_FOR_FAV_COUNTDOWN) {
            if (app.showCountdown) {
                pendingApp = app
                _launchState.value = LaunchState.RequiresCountdown(app)
            } else {
                intentLauncher.openApp(app.info)
            }
        } else {
            val currentPackages = countdownRepository.appPackages.first()

            if (currentPackages.contains(app.info.packageName)) {
                pendingApp = app
                _launchState.value = LaunchState.RequiresCountdown(app)
            } else {
                intentLauncher.openApp(app.info)
            }
        }
    }

    override suspend fun completeLaunch() {
        pendingApp?.let { app -> intentLauncher.openApp(app.info) }
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
