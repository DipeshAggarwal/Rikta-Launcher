package com.lumina.domain.system

import com.lumina.domain.apps.AppInfo
import com.lumina.domain.apps.AppProfile
import kotlinx.coroutines.flow.StateFlow

sealed interface LaunchState {
    data object Idle : LaunchState
    data class RequiresCountdown(val app: AppInfo) : LaunchState
}

interface AppLaunchCoordinator {
    val launchState: StateFlow<LaunchState>

    suspend fun requestLaunch(app: AppInfo)
    suspend fun completeLaunch()
    fun cancelLaunch()
}
