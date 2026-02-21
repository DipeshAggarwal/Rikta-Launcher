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

private const val DEFAULT_SHOW_HIDDEN_APPS_IN_SEARCH = false
private const val DEFAULT_SHOW_SCREEN_TIME_IN_HOME = false
private const val DEFAULT_SHOW_BIG_CLOCK_IN_HOME = false
private const val DEFAULT_SCREEN_TIME_WITH_APP_NAME_KEY = false
private const val DEFAULT_FAVOURITE_BOOST_IN_SEARCH_KEY = false

private const val SHOW_HIDDEN_APPS_IN_SEARCH_KEY = "show_hidden_apps_in_search"
private const val SHOW_SCREEN_TIME_IN_HOME_KEY = "show_screen_time_in_home"
private const val SHOW_BIG_CLOCK_IN_HOME_KEY = "show_big_clock_in_home"
private const val SHOW_SCREEN_TIME_WITH_APP_NAME_KEY = "show_screen_time_with_app_name"
private const val SHOW_FAVOURITE_BOOST_IN_SEARCH_KEY = "show_favourite_boost_in_search"
private const val SPACER_HEIGHT_KEY = "spacer_height"

class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
): SettingsRepository {
    private val showHiddenAppsInSearchKey = booleanPreferencesKey(SHOW_HIDDEN_APPS_IN_SEARCH_KEY)
    private val showScreenTimeInHomeKey = booleanPreferencesKey(SHOW_SCREEN_TIME_IN_HOME_KEY)
    private val showBigClockInHomeKey = booleanPreferencesKey(SHOW_BIG_CLOCK_IN_HOME_KEY)
    private val showScreenTimeWithAppNameKey = booleanPreferencesKey(SHOW_SCREEN_TIME_WITH_APP_NAME_KEY)
    private val showFavouriteBoostInSearchKey = booleanPreferencesKey((SHOW_FAVOURITE_BOOST_IN_SEARCH_KEY))
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

    override suspend fun setScreenTimePageInHome(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[showScreenTimeInHomeKey] = enabled
        }
    }

    override fun showScreenTimePageInHome(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[showScreenTimeInHomeKey] ?: DEFAULT_SHOW_SCREEN_TIME_IN_HOME
        }
    }

    override suspend fun setBigClockInHome(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[showBigClockInHomeKey] = enabled
        }
    }

    override fun showBigClockInHome(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[showBigClockInHomeKey] ?: DEFAULT_SHOW_BIG_CLOCK_IN_HOME
        }
    }

    override suspend fun setScreenTimeVisibleWithApp(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[showScreenTimeWithAppNameKey] = enabled
        }
    }

    override fun showScreenTimeVisibleWithApp(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[showScreenTimeWithAppNameKey] ?: DEFAULT_SCREEN_TIME_WITH_APP_NAME_KEY
        }
    }

    override suspend fun setFavouriteBoostInSearch(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[showFavouriteBoostInSearchKey] = enabled
        }    }

    override fun showFavouriteBoostInSearch(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[showFavouriteBoostInSearchKey] ?: DEFAULT_FAVOURITE_BOOST_IN_SEARCH_KEY
        }    }

    override suspend fun setSearchBoxInAppList(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun showSearchBoxInAppList(): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun setSearchBoxAtBottomInAppList(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun showSearchBoxAtBottomInAppList(): Flow<Boolean> {
        TODO("Not yet implemented")
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
