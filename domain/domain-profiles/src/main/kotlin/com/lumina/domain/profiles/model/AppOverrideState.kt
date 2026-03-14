package com.lumina.domain.profiles.model

import com.lumina.core.model.AppBasicData

data class AppOverrideState(
    val appBasicData: AppBasicData,
    val recommendedUsageMinutes: Int?,
    val customCountdown: Int?
)
