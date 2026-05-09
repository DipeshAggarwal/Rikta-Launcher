package com.lumina.core.database.models

import androidx.room.Embedded
import com.lumina.core.database.entity.ProfileEntity

data class ProfileSummaryRow(
    @Embedded val profile: ProfileEntity,
    val appCount: Int,
    val triggerCount: Int,
    val allowedNotificationCount: Int
)
