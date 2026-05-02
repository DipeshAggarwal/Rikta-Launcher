package com.lumina.domain.profiles.model

import com.lumina.core.model.ProfileAuthMethod
import com.lumina.core.model.ProfileType

data class LauncherProfile(
    val id: String,
    val userHandleNumber: Long,

    val type: ProfileType,
    val name: String,

    val settings: LauncherProfileSettings,
    val overrides: LauncherProfileOverrides
)

data class LauncherProfileSettings(
    val strictMode: Boolean,

    val priorityTriggerLaunch: Boolean,
    val filterNotification: Boolean,

    val blockProfileTriggerSwitching: Boolean,

    val startDnd: Boolean,
    val showAppList: Boolean,
    val hideScreenTimeOnApps: Boolean,
    val disableOnLock: Boolean,

    val blockUnauthorisedApps: Boolean,

    val entryAuthMethod: ProfileAuthMethod,
    val exitAuthMethod: ProfileAuthMethod,
    val activationKey: String?
)

data class LauncherProfileOverrides(
    val background: String?,
    val font: String?,
    val showClock: Boolean?,
    val showBigClock: Boolean?,
    val showDate: Boolean?,
    val showWeather: Boolean?,
    val hideScreenTime: Boolean?
)
