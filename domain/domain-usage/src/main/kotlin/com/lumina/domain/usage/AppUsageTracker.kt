package com.lumina.domain.usage

import com.lumina.core.model.AppBasicData
import kotlinx.coroutines.flow.SharedFlow

interface AppUsageTracker {
    fun onAppForegrounded(
        packageName: String,
        userHandleNumber: Long,
        profileId: String,
        timestamp: Long
    )
    fun onAppBackgrounded(packageName: String, userHandleNumber: Long, timestamp: Long)

    fun onProfileSwitched(newProfileId: String, timestamp: Long)

    fun onBootCompleted(rebootTime: Long)
}
