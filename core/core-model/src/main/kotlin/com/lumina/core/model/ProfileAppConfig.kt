package com.lumina.core.model

data class ProfileAppConfig(
    val appBasicData: AppBasicData,
    val showCountdown: Boolean,
    val recommendedUsageMinutes: Int?
)
