package com.lumina.data.system

import com.lumina.data.countdown.DataStoreCountdownRepository
import com.lumina.domain.apps.AppInfo
import com.lumina.domain.apps.AppProfile
import com.lumina.domain.system.AppLaunchCoordinator
import com.lumina.domain.system.LaunchState
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

@Singleton
class PlatformAppLaunchCoordinator @Inject constructor(
    private val intentLauncher: PlatformIntentLauncher,
    private val countdownRepository: DataStoreCountdownRepository
) : AppLaunchCoordinator{
    private val _launchState = MutableStateFlow<LaunchState>(LaunchState.Idle)
    override val launchState = _launchState.asStateFlow()

    private var pendingApp: AppInfo? = null

    override suspend fun requestLaunch(app: AppInfo) {
        val currentPackages = countdownRepository.countdownAppPackages.first()
        val needsCountdown = currentPackages.contains(app.packageName)

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
