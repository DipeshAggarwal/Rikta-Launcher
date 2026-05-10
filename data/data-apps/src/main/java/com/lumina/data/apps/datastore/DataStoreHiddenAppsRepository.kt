package com.lumina.data.apps.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.lumina.core.datastore.di.SettingsDataStore
import com.lumina.domain.apps.HiddenAppsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject

/**
 * DataStore implementation of [HiddenAppsRepository].
 * Uses a 'StringSet' because the order of hidden apps is usually irrelevant, and Sets provide O(1)
 * lookup performance for visibility checks.
 */
class DataStoreHiddenAppsRepository @Inject constructor(
    @param:SettingsDataStore private val dataStore: DataStore<Preferences>
) : HiddenAppsRepository {
    private val HIDDEN_APPS_KEY = stringSetPreferencesKey("hidden_apps")

    override val appPackages: Flow<Set<String>> = dataStore.data
        .map { prefs -> prefs[HIDDEN_APPS_KEY] ?: emptySet() }

    override suspend fun addApp(packageName: String) {
        dataStore.edit { prefs ->
            val currentHiddenApps = prefs[HIDDEN_APPS_KEY] ?: emptySet()
            val updatedHiddenApps = currentHiddenApps + packageName

            prefs[HIDDEN_APPS_KEY] = updatedHiddenApps
        }
    }

    override suspend fun removeApp(packageName: String) {
        dataStore.edit { prefs ->
            val currentHiddenApps = prefs[HIDDEN_APPS_KEY] ?: emptySet()
            val updatedHiddenApps = currentHiddenApps - packageName

            prefs[HIDDEN_APPS_KEY] = updatedHiddenApps
        }
    }

    override suspend fun setApps(packageNames: List<String>) {
        dataStore.edit { prefs ->
            // Overwrites the entire list (used during database cleanup/sync)
            prefs[HIDDEN_APPS_KEY] = packageNames.toSet()
        }
    }
}
