package com.lumina.domain.coordination

import com.lumina.core.model.LauncherItem
import kotlinx.coroutines.flow.StateFlow

sealed interface LaunchState {
    data object Idle : LaunchState
    data class RequiresCountdown(val app: LauncherItem.App) : LaunchState
}

interface AppLaunchCoordinator {
    val launchState: StateFlow<LaunchState>

    suspend fun requestLaunch(app: LauncherItem.App)
    suspend fun completeLaunch()
    fun cancelLaunch()
}
