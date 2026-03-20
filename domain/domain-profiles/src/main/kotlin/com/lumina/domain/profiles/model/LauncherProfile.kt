package com.lumina.domain.profiles.model

import com.lumina.core.model.ProfileAuthMethod
import com.lumina.core.model.ProfileType

data class LauncherProfile(
    val id: String,
    val userHandleNumber: Long,

    val type: ProfileType,
    val name: String,
    val strictMode: Boolean,

    val priorityTriggerLaunch: Boolean,
    val filterNotification: Boolean,

    val blockProfileTriggerSwitching: Boolean,

    val startDnd: Boolean,
    val showAppList: Boolean,
    val hideScreenTimeOnApps: Boolean,
    val disableOnLock: Boolean,

    val blockUnauthorisedApps: Boolean,

    val overrideBackground: String?,
    val overrideFont: String?,

    val overrideShowClock: Boolean?,
    val overrideShowBigClock: Boolean?,
    val overrideShowDate: Boolean?,
    val overrideShowWeather: Boolean?,

    val overrideHideScreenTime: Boolean?,

    val entryAuthMethod: ProfileAuthMethod,
    val exitAuthMethod: ProfileAuthMethod,
    val activationKey: String?,
)
