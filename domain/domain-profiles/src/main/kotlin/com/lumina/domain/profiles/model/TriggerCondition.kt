package com.lumina.domain.profiles.model

import com.lumina.core.model.LogicalOperator
import com.lumina.core.model.ProfileTriggerType
import java.time.DayOfWeek

data class TriggerCondition(
    val triggerId: Long,
    val profileId: String,
    val sequenceOrder: Int,

    val logicalOperator: LogicalOperator?,
    val triggerType: ProfileTriggerType?,
    val stopIfTrue: Boolean,

    val startTimeMinutes: Int? = null,
    val endTimeMinutes: Int? = null,

    val daysOfWeek: List<Int>? = null,

    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusMeters: Float? = null,

    val wifiSsid: String? = null,
    val bluetoothAddress: String? = null
) {
    companion object {
        fun create(
            isNewTrigger: Boolean,
            triggerId: Long,
            profileId: String,
            sequenceOrder: Int,
            triggerType: ProfileTriggerType,
            logicalOperator: LogicalOperator?,
            stopIfTrue: Boolean,
            startTimeMinutes: Int?,
            endTimeMinutes: Int?,
            daysOfWeek: List<Int>,
            latitude: Double?,
            longitude: Double?,
            radiusMeters: Float?,
            wifiSsid: String?,
            bluetoothAddress: String?
        ): TriggerCondition {
            val resolvedTriggerId = if (isNewTrigger) 0L else triggerId

            return when (triggerType) {
                ProfileTriggerType.TIME -> TriggerCondition(
                    triggerId = resolvedTriggerId,
                    profileId = profileId,
                    sequenceOrder = sequenceOrder,
                    triggerType = ProfileTriggerType.TIME,
                    logicalOperator = logicalOperator,
                    stopIfTrue = stopIfTrue,
                    startTimeMinutes = startTimeMinutes,
                    endTimeMinutes = endTimeMinutes,
                    daysOfWeek = daysOfWeek
                )

                ProfileTriggerType.DAY -> TriggerCondition(
                    triggerId = resolvedTriggerId,
                    profileId = profileId,
                    sequenceOrder = sequenceOrder,
                    triggerType = ProfileTriggerType.DAY,
                    logicalOperator = logicalOperator,
                    stopIfTrue = stopIfTrue,
                    daysOfWeek = daysOfWeek
                )

                ProfileTriggerType.WIFI -> TriggerCondition(
                    triggerId = resolvedTriggerId,
                    profileId = profileId,
                    sequenceOrder = sequenceOrder,
                    triggerType = ProfileTriggerType.WIFI,
                    logicalOperator = logicalOperator,
                    stopIfTrue = stopIfTrue,
                    wifiSsid = wifiSsid
                )

                ProfileTriggerType.BLUETOOTH -> TriggerCondition(
                    triggerId = resolvedTriggerId,
                    profileId = profileId,
                    sequenceOrder = sequenceOrder,
                    triggerType = ProfileTriggerType.BLUETOOTH,
                    logicalOperator = logicalOperator,
                    stopIfTrue = stopIfTrue,
                    bluetoothAddress = bluetoothAddress
                )

                ProfileTriggerType.LOCATION -> TriggerCondition(
                    triggerId = resolvedTriggerId,
                    profileId = profileId,
                    sequenceOrder = sequenceOrder,
                    triggerType = ProfileTriggerType.LOCATION,
                    logicalOperator = logicalOperator,
                    stopIfTrue = stopIfTrue,
                    latitude = latitude,
                    longitude = longitude,
                    radiusMeters = radiusMeters
                )
            }
        }
    }
}
