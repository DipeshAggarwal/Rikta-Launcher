package com.lumina.data.usage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.lumina.domain.usage.UsageSettingsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DEFAULT_RAW_RETENTION_DAYS = 14

class DataStoreUsageSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UsageSettingsRepository {
    private val RAW_RETENTION_DAYS_KEY = intPreferencesKey("usage_raw_retention_days")

    override val rawRetentionDays: Flow<Int>
        get() = dataStore.data.map { prefs -> prefs[RAW_RETENTION_DAYS_KEY] ?: DEFAULT_RAW_RETENTION_DAYS }

    override suspend fun setRawRetentionDays(days: Int) {
        require(days > 0) { "Retention days must be positive." }
        dataStore.edit { it[RAW_RETENTION_DAYS_KEY] = days }
    }
}