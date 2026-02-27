package com.lumina.domain.system

import com.lumina.domain.apps.AppInfo
import com.lumina.domain.apps.AppShortcut

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
    suspend fun openAlarm(): LaunchResult
    suspend fun openCalendar(): LaunchResult
}
