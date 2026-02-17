package com.lumina.data.apps.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.lumina.domain.apps.HiddenAppsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject

private const val HIDDEN_APPS_KEY = "hidden_apps"

/**
 * DataStore implementation of [HiddenAppsRepository].
 * Uses a 'StringSet' because the order of hidden apps is usually irrelevant, and Sets provide O(1)
 * lookup performance for visibility checks.
 */
class DataStoreHiddenAppsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
): HiddenAppsRepository {
    private val hiddenAppsKey = stringSetPreferencesKey(HIDDEN_APPS_KEY)

    override suspend fun addHiddenApp(packageName: String) {
        dataStore.edit { prefs ->
            val currentHiddenApps = prefs[hiddenAppsKey] ?: emptySet()
            val updatedHiddenApps = currentHiddenApps + packageName

            prefs[hiddenAppsKey] = updatedHiddenApps
        }
    }

    override suspend fun removeHiddenApp(packageName: String) {
        dataStore.edit { prefs ->
            val currentHiddenApps = prefs[hiddenAppsKey] ?: emptySet()
            val updatedHiddenApps = currentHiddenApps - packageName

            prefs[hiddenAppsKey] = updatedHiddenApps
        }
    }

    override suspend fun setHiddenApps(packageNames: List<String>) {
        dataStore.edit { prefs ->
            // Overwrites the entire list (used during database cleanup/sync)
            prefs[hiddenAppsKey] = packageNames.toSet()
        }
    }

    override fun allHiddenApps(): Flow<Set<String>> {
        return dataStore.data.map { prefs -> prefs[hiddenAppsKey] ?: emptySet() }
    }

}
