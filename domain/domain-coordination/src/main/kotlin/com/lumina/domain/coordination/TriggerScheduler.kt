package com.lumina.domain.coordination

interface TriggerScheduler {
    suspend fun refresh()
}
