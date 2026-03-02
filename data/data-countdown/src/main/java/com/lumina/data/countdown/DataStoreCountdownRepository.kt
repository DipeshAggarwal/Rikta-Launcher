package com.lumina.data.countdown

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.lumina.domain.countdown.CountdownAppsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreCountdownRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : CountdownAppsRepository {
    private val COUNTDOWN_APPS_KEY = stringSetPreferencesKey("countdown_apps")

    override val appPackages: Flow<Set<String>> = dataStore.data
        .map { prefs -> prefs[COUNTDOWN_APPS_KEY] ?: emptySet() }

    override suspend fun addApp(packageName: String) {
        dataStore.edit { prefs ->
            val currentApps = prefs[COUNTDOWN_APPS_KEY] ?: emptySet()
            val updatedApps = currentApps + packageName

            prefs[COUNTDOWN_APPS_KEY] = updatedApps
        }
    }

    override suspend fun removeApp(packageName: String) {
        dataStore.edit { prefs ->
            val currentApps = prefs[COUNTDOWN_APPS_KEY] ?: emptySet()
            val updatedApps = currentApps - packageName

            prefs[COUNTDOWN_APPS_KEY] = updatedApps
        }
    }

    override suspend fun setApps(packageNames: List<String>) {
        dataStore.edit { prefs ->
            // Overwrites the entire list (used during database cleanup/sync)
            prefs[COUNTDOWN_APPS_KEY] = packageNames.toSet()
        }
    }
}
