package com.lumina.domain.profiles.model

data class LauncherProfile(
    val id: String,
    val userHandleNumber: Long,
    val type: ProfileType,
    val name: String,
    val strictMode: Boolean,

    val priorityTriggerLaunch: Boolean,
    val filterNotification: Boolean,
    val startDnd: Boolean,
    val showAppList: Boolean,

    val overrideBackground: String?,
    val overrideFont: String?,

    val overrideShowClock: Boolean?,
    val overrideShowBigClock: Boolean?,
    val overrideShowDate: Boolean?,
    val overrideShowWeather: Boolean?,

    val activationKey: String?,
    val requiresBiometric: Boolean?
)
