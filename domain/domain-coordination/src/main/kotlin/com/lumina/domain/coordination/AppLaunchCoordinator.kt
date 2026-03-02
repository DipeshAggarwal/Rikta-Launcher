package com.lumina.domain.coordination

import com.lumina.core.model.AppInfo
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
