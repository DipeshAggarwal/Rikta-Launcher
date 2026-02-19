package com.lumina.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.lumina.core.common.AppDefaults.DEFAULT_SPACER_HEIGHT
import com.lumina.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject

private const val SHOW_HIDDEN_APPS_IN_SEARCH_KEY = "show_hidden_apps_in_search"
private const val SPACER_HEIGHT_KEY = "spacer_height"
private const val DEFAULT_SHOW_HIDDEN_APPS_IN_SEARCH = false

class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
): SettingsRepository {
    private val showHiddenAppsInSearchKey = booleanPreferencesKey(SHOW_HIDDEN_APPS_IN_SEARCH_KEY)
    private val spacerHeightKey = intPreferencesKey(SPACER_HEIGHT_KEY)

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

    override suspend fun setSpacerHeight(height: Int) {
        dataStore.edit { prefs ->
            prefs[spacerHeightKey] = height
        }
    }

    override suspend fun resetSpacerHeight() {
        dataStore.edit { prefs ->
            prefs[spacerHeightKey] = DEFAULT_SPACER_HEIGHT
        }
    }

    override fun spacerHeight(): Flow<Int> {
        return dataStore.data.map { prefs ->
            prefs[spacerHeightKey] ?: DEFAULT_SPACER_HEIGHT
        }
    }
}
