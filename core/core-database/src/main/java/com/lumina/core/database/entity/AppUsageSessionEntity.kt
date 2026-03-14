package com.lumina.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

private const val VERSION = 1

@Entity(
    tableName = "app_usage_sessions",
    indices = [
        Index(value = ["profileId", "startTime"]),
        Index(value = ["packageName", "userHandleNumber", "startTime"])
    ]
)
data class AppUsageSessionEntity(
    @PrimaryKey val sessionId: String,
    val profileId: String,
    val packageName: String,
    val userHandleNumber: Long,

    val startTime : Long,
    val endTime: Long?,

    val version: Int = VERSION
)
