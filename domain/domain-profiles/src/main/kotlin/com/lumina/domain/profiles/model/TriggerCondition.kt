package com.lumina.domain.profiles.model

import com.lumina.core.model.LogicalOperator
import com.lumina.core.model.ProfileTriggerType

data class TriggerCondition(
    val triggerId: Long,
    val profileId: String,
    val sequenceOrder: Int,

    val logicalOperator: LogicalOperator?,
    val triggerType: ProfileTriggerType?,
    val stopIfTrue: Boolean,

    val startTimeMinutes: Int?,
    val endTimeMinutes: Int?,

    val daysOfWeek: List<Int>?,

    val latitude: Double?,
    val longitude: Double?,
    val radiusMeters: Float?,

    val wifiSsid: String?,
    val bluetoothAddress: String?
)
