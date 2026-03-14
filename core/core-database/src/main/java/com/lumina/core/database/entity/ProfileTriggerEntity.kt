package com.lumina.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lumina.core.model.LogicalOperator
import com.lumina.core.model.ProfileTriggerType

private const val VERSION = 1

@Entity(
    tableName = "profile_triggers",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
data class ProfileTriggerEntity(
    @PrimaryKey(autoGenerate = true) val triggerId: Long = 0,
    val profileId: String,
    val sequenceOrder: Int,

    val logicalOperator: LogicalOperator? = null,
    val triggerType: ProfileTriggerType? = null,
    val stopIfTrue: Boolean = false,

    val startTimeMinutes: Int? = null,
    val endTimeMinutes: Int? = null,

    val daysOfWeek: String? = null,

    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusMeters: Float? = null,

    val wifiSsid: String? = null,
    val bluetoothAddress: String? = null,

    val version: Int = VERSION
)
