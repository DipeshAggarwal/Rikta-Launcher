package com.lumina.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.lumina.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject

private const val SHOW_HIDDEN_APPS_IN_SEARCH_KEY = "show_hidden_apps_in_search"
private const val DEFAULT_SHOW_HIDDEN_APPS_IN_SEARCH = false

class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
): SettingsRepository {
    private val showHiddenAppsInSearchKey = booleanPreferencesKey(SHOW_HIDDEN_APPS_IN_SEARCH_KEY)

    override suspend fun setShowHiddenAppsInSearch(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[showHiddenAppsInSearchKey] = enabled
        }
    }

    override fun showHiddenAppsInSearch(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[showHiddenAppsInSearchKey] ?: DEFAULT_SHOW_HIDDEN_APPS_IN_SEARCH
        }
    }
}
