package com.lumina.domain.coordination.usecase

import com.lumina.domain.coordination.model.ResolvedPermissionsState
import com.lumina.domain.coordination.model.ResolvedThemeState
import com.lumina.domain.coordination.model.ResolvedUIState
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.domain.settings.HomeSettings
import com.lumina.domain.settings.SettingsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetActiveHomeConfigUseCase @Inject constructor (
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(): Flow<ResolvedUIState> {
        return combine(
            settingsRepository.allSettings,
            profileRepository.activeProfile
        ) { baseSettings, activeProfile ->
            if (activeProfile == null) return@combine ResolvedUIState(
                home = baseSettings.home,
                appList = baseSettings.appList,
                search = baseSettings.search,
                countdown = baseSettings.countdown,
                layout = baseSettings.layout,
                theme = ResolvedThemeState(
                    background = null,
                    font = null
                ),
                profilePermissions = ResolvedPermissionsState(
                    allowLauncherSettingsChange = true,
                    allowManagingApps = true,
                    allowLauncherAppActions = true,
                    allowProfileManagement = false
                )
            )

            val themeState = ResolvedThemeState(
                background = activeProfile.overrides.background,
                font = activeProfile.overrides.font
            )
            val permissionState = ResolvedPermissionsState(
                allowLauncherSettingsChange = activeProfile.permissions.allowLauncherSettingsChange,
                allowManagingApps = activeProfile.permissions.allowManagingApps,
                allowLauncherAppActions = activeProfile.permissions.allowLauncherAppActions,
                allowProfileManagement = activeProfile.permissions.allowProfileManagement
            )

            ResolvedUIState(
                home = baseSettings.home.applyProfile(activeProfile),
                appList = baseSettings.appList,
                search = baseSettings.search,
                countdown = baseSettings.countdown,
                layout = baseSettings.layout,
                theme = themeState,
                profilePermissions = permissionState
            )
        }
    }
}

private fun HomeSettings.applyProfile(profile: LauncherProfile): HomeSettings {
    val hideScreenTime = profile.overrides.hideScreenTime == true

    return copy(
        showClock = profile.overrides.showClock ?: showClock,
        showBigClock = profile.overrides.showBigClock ?: showBigClock,
        showDate = profile.overrides.showDate ?: showDate,
        showScreenTimePage = !hideScreenTime && showScreenTimePage,
        showScreenTimeWithAppName = !hideScreenTime && showScreenTimeWithAppName
    )
}
