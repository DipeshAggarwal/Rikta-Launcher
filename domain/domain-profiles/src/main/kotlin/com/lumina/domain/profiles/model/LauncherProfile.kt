package com.lumina.domain.profiles.model

import com.lumina.core.model.AppCategory
import com.lumina.core.model.AppUsageEnforcementMode
import com.lumina.core.model.ProfileAuthMethod
import com.lumina.core.model.ProfileBackground
import com.lumina.core.model.ProfileType

data class LauncherProfile(
    val id: String,
    val userHandleNumber: Long,

    val type: ProfileType,
    val name: String,
    val description: String?,

    val createdAt: Long,
    val updatedAt: Long,

    val isAdmin: Boolean,
    val priorityTriggerLaunch: Boolean,

    val settings: LauncherProfileSettings,
    val permissions: LauncherProfilePermissions,
    val restrictions: LauncherProfileRestrictions,
    val auth: LauncherProfileAuth,
    val overrides: LauncherProfileOverrides
)

data class LauncherProfileSettings(
    val startDnd: Boolean,
    val hideScreenTimeOnApps: Boolean,
    val maxAppCount: Int,
    val autoAddCategoryApps: List<AppCategory>
)

data class LauncherProfilePermissions(
    val allowLauncherSettingsChange: Boolean,
    val allowManagingApps: Boolean,
    val allowLauncherAppActions: Boolean,
    val allowProfileManagement: Boolean
)

data class LauncherProfileRestrictions(
    val appUsageEnforcementMode: AppUsageEnforcementMode,
    val blockAppList: Boolean,
    val blockProfileTriggerSwitching: Boolean,
    val blockUnauthorisedApps: Boolean,
    val blockRecentApps: Boolean,
    val blockSystemAppAdd: Boolean,
    val blockNotificationShade: Boolean,
    val filterNotifications: Boolean,
    val switchOnDeviceLock: Boolean
)

data class LauncherProfileAuth(
    val entryAuthMethod: ProfileAuthMethod,
    val exitAuthMethod: ProfileAuthMethod,
    val activationKey: String?
)

data class LauncherProfileOverrides(
    val theme: String?,
    val background: ProfileBackground?,
    val font: String?,
    val showClock: Boolean?,
    val showBigClock: Boolean?,
    val showDate: Boolean?,
    val showWeather: Boolean?,
    val hideScreenTime: Boolean?,
    val iconName: String?
)
