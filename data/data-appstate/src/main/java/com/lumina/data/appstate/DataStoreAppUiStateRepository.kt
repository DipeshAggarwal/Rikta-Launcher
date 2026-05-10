package com.lumina.data.appstate

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.lumina.core.datastore.di.AppUiStateDataStore
import com.lumina.domain.appstate.AppUiStateRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreAppUiStateRepository @Inject constructor(
    @param:AppUiStateDataStore private val dataStore: DataStore<Preferences>,
) : AppUiStateRepository {
    private object Keys {
        val PROFILES_INFO_BANNER_DISMISSED = booleanPreferencesKey("profiles_info_banner")
    }
    override val showProfilesInfoBanner: Flow<Boolean>
        get() = dataStore.data.map { prefs ->
            prefs[Keys.PROFILES_INFO_BANNER_DISMISSED] != true
        }
    override suspend fun dismissProfilesInfoBanner() {
        dataStore.edit { prefs ->
            prefs[Keys.PROFILES_INFO_BANNER_DISMISSED] = true
        }
    }
}
