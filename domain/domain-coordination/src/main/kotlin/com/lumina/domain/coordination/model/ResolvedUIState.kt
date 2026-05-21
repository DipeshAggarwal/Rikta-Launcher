package com.lumina.domain.coordination.model

import com.lumina.core.model.ProfileBackground
import com.lumina.domain.settings.AppListSettings
import com.lumina.domain.settings.CountdownSettings
import com.lumina.domain.settings.HomeSettings
import com.lumina.domain.settings.LayoutSettings
import com.lumina.domain.settings.SearchSettings

data class ResolvedUIState(
    val home: HomeSettings,
    val appList: AppListSettings,
    val search: SearchSettings,
    val countdown: CountdownSettings,
    val layout: LayoutSettings,
    val theme: ResolvedThemeState,

    val profilePermissions: ResolvedPermissionsState
)

data class ResolvedPermissionsState(
    val allowLauncherSettingsChange: Boolean,
    val allowManagingApps: Boolean,
    val allowLauncherAppActions: Boolean,
    val allowProfileManagement: Boolean
)

data class ResolvedThemeState(
    val background: ProfileBackground?,
    val font: String?
)
