package com.lumina.domain.coordination

import com.lumina.core.model.AppInfo
import com.lumina.core.model.AppShortcut
import com.lumina.core.model.LauncherItem

sealed interface LaunchResult {
    data object Success: LaunchResult
    data object NoLaunchIntent: LaunchResult
    data class Error(val exception: Exception) : LaunchResult
}

interface IntentLauncher {
    suspend fun openApp(app: AppInfo): LaunchResult
    suspend fun openAppInfo(app: AppInfo): LaunchResult
    suspend fun uninstallApp(app: AppInfo): LaunchResult
    suspend fun launchShortcut(shortcut: AppShortcut): LaunchResult
    suspend fun launchProfileShortcut(shortcut: LauncherItem.Shortcut): LaunchResult
    suspend fun openAlarm(): LaunchResult
    suspend fun openCalendar(): LaunchResult
}
