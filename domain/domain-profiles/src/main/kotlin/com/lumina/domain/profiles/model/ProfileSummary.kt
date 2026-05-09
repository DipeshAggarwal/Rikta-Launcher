package com.lumina.domain.profiles.model

data class ProfileSummary(
    val profile: LauncherProfile,
    val appCount: Int,
    val triggerCount: Int,
    val allowedNotificationCount: Int
)
