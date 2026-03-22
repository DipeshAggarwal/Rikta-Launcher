package com.lumina.data.usage

import jakarta.inject.Inject
import kotlinx.coroutines.delay

interface HeartbeatTicker {
    suspend fun awaitNextTick()
}

class DelayHeartbeatTicker @Inject constructor() : HeartbeatTicker {
    override suspend fun awaitNextTick() {
        delay(UsageConstants.HEARTBEAT_INTERVAL_MILLISECONDS)
    }
}
