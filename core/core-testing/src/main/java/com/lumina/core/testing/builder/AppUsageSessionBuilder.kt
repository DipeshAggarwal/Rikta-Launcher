package com.lumina.core.testing.builder

import com.lumina.core.database.entity.AppUsageSessionEntity
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object AppUsageSessionBuilder {
    @OptIn(ExperimentalUuidApi::class)
    fun build(
        sessionId: String = Uuid.random().toString(),
        profileId: String = "profile_test",
        packageName: String = "com.example.app",
        userHandleNumber: Long = 0L,
        startTime: Long = 1000L,
        endTime: Long? = 2000L
    ) = AppUsageSessionEntity(
        sessionId = sessionId,
        profileId = profileId,
        packageName = packageName,
        userHandleNumber = userHandleNumber,
        startTime = startTime,
        endTime = endTime
    )
}
