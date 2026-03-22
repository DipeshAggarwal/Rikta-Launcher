package com.lumina.data.usage

import kotlinx.coroutines.Job

data class ActiveSession(
    val sessionId: String,
    val packageName: String,
    val userHandleNumber: Long,
    val profileId: String,
    val startTime: Long,

    @Volatile var writtenToDb: Boolean = false
)