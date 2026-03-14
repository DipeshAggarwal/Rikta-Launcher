package com.lumina.data.usage

import kotlinx.coroutines.Job

data class ActiveSession(
    val sessionId: String,
    val packageName: String,
    val userHandleNumber: Long,
    val profileId: String,
    val startTime: Long,

    var heartbeatJob: Job? = null,
    var writtenToDb: Boolean = false
)