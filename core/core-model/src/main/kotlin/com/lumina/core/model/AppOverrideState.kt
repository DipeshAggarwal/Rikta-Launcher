package com.lumina.core.model

data class AppOverrideState(
    val appBasicData: AppBasicData,
    val favouriteOrder: Int?,
    val showCountdown: Boolean,
    val recommendedUsageMinutes: Int?
)
