package com.lumina.feature.system.triggers.builder

import com.lumina.core.model.LogicalOperator
import com.lumina.core.model.ProfileTriggerType
import com.lumina.domain.profiles.model.TriggerCondition

object TriggerConditionBuilder {

    fun wifi(
        triggerId: Long = 1L,
        profileId: String = "profile_test",
        sequenceOrder: Int = 0,
        logicalOperator: LogicalOperator? = null,
        stopIfTrue: Boolean = false,
        wifiSsid: String = "RiktaWifi",
    ) = TriggerCondition(
        triggerId = triggerId,
        profileId = profileId,
        sequenceOrder = sequenceOrder,
        logicalOperator = logicalOperator,
        triggerType = ProfileTriggerType.WIFI,
        stopIfTrue = stopIfTrue,
        startTimeMinutes = null,
        endTimeMinutes = null,
        daysOfWeek = null,
        latitude = null,
        longitude = null,
        radiusMeters = null,
        wifiSsid = wifiSsid,
        bluetoothAddress = null
    )

    fun bluetooth(
        triggerId: Long = 2L,
        profileId: String = "profile_test",
        sequenceOrder: Int = 1,
        logicalOperator: LogicalOperator? = null,
        stopIfTrue: Boolean = false,
        bluetoothAddress: String = "AA:BB:CC:DD:EE:FF",
    ) = TriggerCondition(
        triggerId = triggerId,
        profileId = profileId,
        sequenceOrder = sequenceOrder,
        logicalOperator = logicalOperator,
        triggerType = ProfileTriggerType.BLUETOOTH,
        stopIfTrue = stopIfTrue,
        startTimeMinutes = null,
        endTimeMinutes = null,
        daysOfWeek = null,
        latitude = null,
        longitude = null,
        radiusMeters = null,
        wifiSsid = null,
        bluetoothAddress = bluetoothAddress
    )

    fun location(
        triggerId: Long = 3L,
        profileId: String = "profile_test",
        sequenceOrder: Int = 2,
        logicalOperator: LogicalOperator? = null,
        stopIfTrue: Boolean = false,
        latitude: Double = 37.7749,
        longitude: Double = -122.4194,
        radiusMeters: Float = 100f
    ) = TriggerCondition(
        triggerId = triggerId,
        profileId = profileId,
        sequenceOrder = sequenceOrder,
        logicalOperator = logicalOperator,
        triggerType = ProfileTriggerType.LOCATION,
        stopIfTrue = stopIfTrue,
        startTimeMinutes = null,
        endTimeMinutes = null,
        daysOfWeek = null,
        latitude = latitude,
        longitude = longitude,
        radiusMeters = radiusMeters,
        wifiSsid = null,
        bluetoothAddress = null
    )

    fun time(
        triggerId: Long = 4L,
        profileId: String = "profile_test",
        sequenceOrder: Int = 3,
        logicalOperator: LogicalOperator? = null,
        stopIfTrue: Boolean = false,
        startTimeMinutes: Int? = null,
        endTimeMinutes: Int? = null,
        daysOfWeek: List<Int>? = null,
    ) = TriggerCondition(
        triggerId = triggerId,
        profileId = profileId,
        sequenceOrder = sequenceOrder,
        logicalOperator = logicalOperator,
        triggerType = ProfileTriggerType.TIME,
        stopIfTrue = stopIfTrue,
        startTimeMinutes = startTimeMinutes,
        endTimeMinutes = endTimeMinutes,
        daysOfWeek = daysOfWeek,
        latitude = null,
        longitude = null,
        radiusMeters = null,
        wifiSsid = null,
        bluetoothAddress = null
    )
}