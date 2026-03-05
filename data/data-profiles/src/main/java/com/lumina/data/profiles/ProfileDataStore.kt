package com.lumina.data.profiles

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val ACTIVE_PROFILE_ID_KEY = stringPreferencesKey("active_profile_id")

    val activeProfileId: Flow<String?> = dataStore.data.map { prefs ->
        prefs[ACTIVE_PROFILE_ID_KEY]
    }

    suspend fun setActiveProfileId(id: String?) {
        dataStore.edit { prefs ->
            if (id != null) prefs[ACTIVE_PROFILE_ID_KEY] = id
            else prefs.remove(ACTIVE_PROFILE_ID_KEY)
        }
    }
}
