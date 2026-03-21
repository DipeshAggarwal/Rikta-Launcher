package com.lumina.core.testing.builder

import com.lumina.core.database.entity.ProfileTriggerEntity
import com.lumina.core.model.LogicalOperator
import com.lumina.core.model.ProfileTriggerType

object ProfileTriggerEntityBuilder {
    fun build(
        triggerId: Long = 1L,
        profileId: String = "profile_test",
        sequenceOrder: Int = 0,
        daysOfWeek: String? = null
    ) = ProfileTriggerEntity(
        triggerId = triggerId,
        profileId = profileId,
        sequenceOrder = sequenceOrder,
        logicalOperator = LogicalOperator.AND,
        triggerType = ProfileTriggerType.WIFI,
        stopIfTrue = false,
        startTimeMinutes = null,
        endTimeMinutes = null,
        daysOfWeek = daysOfWeek,
        latitude = null,
        longitude = null,
        radiusMeters = null,
        wifiSsid = "RiktaWifi",
        bluetoothAddress = null,
    )
}
