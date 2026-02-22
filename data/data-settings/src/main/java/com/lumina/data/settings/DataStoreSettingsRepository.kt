package com.lumina.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lumina.domain.settings.AppListSettings
import com.lumina.domain.settings.AppsAlignment
import com.lumina.domain.settings.HomeSettings
import com.lumina.domain.settings.LayoutSettings
import com.lumina.domain.settings.SearchSettings
import com.lumina.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject

class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
): SettingsRepository {
    private object Keys {
        // Home
        val SHOW_SCREEN_TIME_PAGE = booleanPreferencesKey("show_screen_time_in_home")
        val SHOW_BIG_CLOCK = booleanPreferencesKey("show_big_clock_in_home")
        val SHOW_SCREEN_TIME_WITH_APP_NAME = booleanPreferencesKey("show_screen_time_with_app_name")

        // AppList
        val SHOW_SEARCH_BOX = booleanPreferencesKey("show_search_box_in_app_list")
        val SHOW_SEARCH_BOX_AT_BOTTOM = booleanPreferencesKey("show_search_box_at_bottom_in_app_list")
        val APPS_ALIGNMENT = stringPreferencesKey("apps_list_alignment")

        // Search
        val SHOW_HIDDEN_APPS_IN_SEARCH = booleanPreferencesKey("show_hidden_apps_in_search")
        val FAVOURITE_BOOST_IN_SEARCH = booleanPreferencesKey("show_favourite_boost_in_search")

        // Layout
        val SPACER_HEIGHT = intPreferencesKey("spacer_height")
    }

    override val homeSettings: Flow<HomeSettings> = dataStore.data.map { prefs ->
        HomeSettings(
            showScreenTimePage = prefs[Keys.SHOW_SCREEN_TIME_PAGE] ?: HomeSettings().showScreenTimePage,
            showBigClock = prefs[Keys.SHOW_BIG_CLOCK] ?: HomeSettings().showBigClock,
            showScreenTimeWithAppName = prefs[Keys.SHOW_SCREEN_TIME_WITH_APP_NAME] ?: HomeSettings().showScreenTimeWithAppName
        )
    }
    override val appListSettings: Flow<AppListSettings> = dataStore.data.map { prefs ->
        AppListSettings(
            showSearchBox = prefs[Keys.SHOW_SEARCH_BOX] ?: AppListSettings().showSearchBox,
            showSearchBoxAtBottom = prefs[Keys.SHOW_SEARCH_BOX_AT_BOTTOM] ?: AppListSettings().showSearchBoxAtBottom,
            appsListAlignment = prefs[Keys.APPS_ALIGNMENT]?.let {
                AppsAlignment.valueOf(it)
            } ?: AppListSettings().appsListAlignment
        )
    }
    override val searchSettings: Flow<SearchSettings> = dataStore.data.map { prefs ->
        SearchSettings(
            showHiddenAppsInSearch = prefs[Keys.SHOW_HIDDEN_APPS_IN_SEARCH] ?: SearchSettings().showHiddenAppsInSearch,
            favouriteBoostInSearch = prefs[Keys.FAVOURITE_BOOST_IN_SEARCH] ?: SearchSettings().favouriteBoostInSearch
        )
    }
    override val layoutSettings: Flow<LayoutSettings> = dataStore.data.map { prefs ->
        LayoutSettings(
            spacerHeight = prefs[Keys.SPACER_HEIGHT] ?: LayoutSettings().spacerHeight
        )
    }

    override suspend fun updateHomeSettings(update: HomeSettings.() -> HomeSettings) {
        dataStore.edit { prefs ->
            val current = HomeSettings(
                showScreenTimePage = prefs[Keys.SHOW_SCREEN_TIME_PAGE] ?: HomeSettings().showScreenTimePage,
                showBigClock = prefs[Keys.SHOW_BIG_CLOCK] ?: HomeSettings().showBigClock,
                showScreenTimeWithAppName = prefs[Keys.SHOW_SCREEN_TIME_WITH_APP_NAME] ?: HomeSettings().showScreenTimeWithAppName
            )

            val updated = current.update()
            prefs[Keys.SHOW_SCREEN_TIME_PAGE] = updated.showScreenTimePage
            prefs[Keys.SHOW_BIG_CLOCK] = updated.showBigClock
            prefs[Keys.SHOW_SCREEN_TIME_WITH_APP_NAME] = updated.showScreenTimeWithAppName
        }
    }

    override suspend fun updateAppListSettings(update: AppListSettings.() -> AppListSettings) {
        dataStore.edit { prefs ->
            val current = AppListSettings(
                showSearchBox = prefs[Keys.SHOW_SEARCH_BOX] ?: AppListSettings().showSearchBox,
                showSearchBoxAtBottom = prefs[Keys.SHOW_SEARCH_BOX_AT_BOTTOM] ?: AppListSettings().showSearchBoxAtBottom,
                appsListAlignment = prefs[Keys.APPS_ALIGNMENT]?.let {
                    AppsAlignment.valueOf(it)
                } ?: AppListSettings().appsListAlignment
            )

            val updated = current.update()
            prefs[Keys.SHOW_SEARCH_BOX] = updated.showSearchBox
            prefs[Keys.SHOW_SEARCH_BOX_AT_BOTTOM] = updated.showSearchBoxAtBottom
            prefs[Keys.APPS_ALIGNMENT] = updated.appsListAlignment.name
        }
    }

    override suspend fun updateSearchSettings(update: SearchSettings.() -> SearchSettings) {
        dataStore.edit { prefs ->
            val current = SearchSettings(
                showHiddenAppsInSearch = prefs[Keys.SHOW_HIDDEN_APPS_IN_SEARCH] ?: SearchSettings().showHiddenAppsInSearch,
                favouriteBoostInSearch = prefs[Keys.FAVOURITE_BOOST_IN_SEARCH] ?: SearchSettings().favouriteBoostInSearch
            )

            val updated = current.update()
            prefs[Keys.SHOW_HIDDEN_APPS_IN_SEARCH] = updated.showHiddenAppsInSearch
            prefs[Keys.FAVOURITE_BOOST_IN_SEARCH] = updated.favouriteBoostInSearch
        }
    }

    override suspend fun updateLayoutSettings(update: LayoutSettings.() -> LayoutSettings) {
        dataStore.edit { prefs ->
            val current = LayoutSettings(
                spacerHeight = prefs[Keys.SPACER_HEIGHT] ?: LayoutSettings().spacerHeight
            )

            val updated = current.update()
            prefs[Keys.SPACER_HEIGHT] = updated.spacerHeight
        }
    }
}
