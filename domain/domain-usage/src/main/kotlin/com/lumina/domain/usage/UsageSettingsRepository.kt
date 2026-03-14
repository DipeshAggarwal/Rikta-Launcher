package com.lumina.domain.usage

import kotlinx.coroutines.flow.Flow

interface UsageSettingsRepository {
    val rawRetentionDays: Flow<Int>

    suspend fun setRawRetentionDays(days: Int)
}
