package com.lumina.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lumina.domain.settings.AppListSettings
import com.lumina.domain.settings.AppAlignment
import com.lumina.domain.settings.HomeAlignment
import com.lumina.domain.settings.HomeSettings
import com.lumina.domain.settings.HomeVerticalAlignment
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
        val SHOW_CLOCK = booleanPreferencesKey("show_clock_in_home")
        val SHOW_BIG_CLOCK = booleanPreferencesKey("show_big_clock_in_home")
        val SHOW_SCREEN_TIME_WITH_APP_NAME = booleanPreferencesKey("show_screen_time_with_app_name")
        val USE_TWELVE_HOUR_DISPLAY = booleanPreferencesKey("use_twelve_hour_display")
        val SHOW_DATE = booleanPreferencesKey("show_date")
        val SHOW_BIG_DATE = booleanPreferencesKey("show_big_date")
        val HOME_ALIGNMENT = stringPreferencesKey("home_apps_alignment")
        val HOME_VERTICAL_ALIGNMENT = stringPreferencesKey("home_vertical_alignment")
        val SHOW_FIRST_TIME_HELP = booleanPreferencesKey("show_first_time_help")


        // AppList
        val SHOW_SEARCH_BOX = booleanPreferencesKey("show_search_box_in_app_list")
        val SHOW_SEARCH_BOX_AT_BOTTOM = booleanPreferencesKey("show_search_box_at_bottom_in_app_list")
        val APPS_ALIGNMENT = stringPreferencesKey("apps_list_alignment")
        val AUTO_FOCUS_SEARCH = booleanPreferencesKey("auto_focus_search")

        // Search
        val SHOW_HIDDEN_APPS_IN_SEARCH = booleanPreferencesKey("show_hidden_apps_in_search")
        val FAVOURITE_BOOST_IN_SEARCH = booleanPreferencesKey("show_favourite_boost_in_search")
        val AUTO_OPEN_ON_SEARCH = booleanPreferencesKey("auto_open_on_search")

        // Layout
        val SPACER_HEIGHT = intPreferencesKey("spacer_height")
    }

    override val homeSettings: Flow<HomeSettings> = dataStore.data.map { prefs ->
        HomeSettings(
            showScreenTimePage = prefs[Keys.SHOW_SCREEN_TIME_PAGE] ?: HomeSettings().showScreenTimePage,
            showClock = prefs[Keys.SHOW_CLOCK] ?: HomeSettings().showClock,
            showBigClock = prefs[Keys.SHOW_BIG_CLOCK] ?: HomeSettings().showBigClock,
            useTwelveHourDisplay = prefs[Keys.USE_TWELVE_HOUR_DISPLAY] ?: HomeSettings().useTwelveHourDisplay,
            showDate = prefs[Keys.SHOW_DATE] ?: HomeSettings().showDate,
            showBigDate = prefs[Keys.SHOW_BIG_DATE] ?: HomeSettings().showBigDate,
            showScreenTimeWithAppName = prefs[Keys.SHOW_SCREEN_TIME_WITH_APP_NAME] ?: HomeSettings().showScreenTimeWithAppName,
            homeAlignment = prefs[Keys.HOME_ALIGNMENT]?.let {
                HomeAlignment.valueOf(it)
            } ?: HomeSettings().homeAlignment,
            homeVerticalAlignment = prefs[Keys.HOME_VERTICAL_ALIGNMENT]?.let {
                HomeVerticalAlignment.valueOf(it)
            } ?: HomeSettings().homeVerticalAlignment,
            showFirstTimeHelp = prefs[Keys.SHOW_FIRST_TIME_HELP] ?: HomeSettings().showFirstTimeHelp
        )
    }
    override val appListSettings: Flow<AppListSettings> = dataStore.data.map { prefs ->
        AppListSettings(
            showSearchBox = prefs[Keys.SHOW_SEARCH_BOX] ?: AppListSettings().showSearchBox,
            showSearchBoxAtBottom = prefs[Keys.SHOW_SEARCH_BOX_AT_BOTTOM] ?: AppListSettings().showSearchBoxAtBottom,
            appsListAlignment = prefs[Keys.APPS_ALIGNMENT]?.let {
                AppAlignment.valueOf(it)
            } ?: AppListSettings().appsListAlignment,
            autoFocusSearch = prefs[Keys.AUTO_FOCUS_SEARCH] ?: AppListSettings().autoFocusSearch
        )
    }
    override val searchSettings: Flow<SearchSettings> = dataStore.data.map { prefs ->
        SearchSettings(
            showHiddenAppsInSearch = prefs[Keys.SHOW_HIDDEN_APPS_IN_SEARCH] ?: SearchSettings().showHiddenAppsInSearch,
            favouriteBoostInSearch = prefs[Keys.FAVOURITE_BOOST_IN_SEARCH] ?: SearchSettings().favouriteBoostInSearch,
            autoOpenOnSearch = prefs[Keys.AUTO_OPEN_ON_SEARCH] ?: SearchSettings().autoOpenOnSearch,
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
                showClock = prefs[Keys.SHOW_CLOCK] ?: HomeSettings().showClock,
                showBigClock = prefs[Keys.SHOW_BIG_CLOCK] ?: HomeSettings().showBigClock,
                useTwelveHourDisplay = prefs[Keys.USE_TWELVE_HOUR_DISPLAY] ?: HomeSettings().useTwelveHourDisplay,
                showDate = prefs[Keys.SHOW_DATE] ?: HomeSettings().showDate,
                showBigDate = prefs[Keys.SHOW_BIG_DATE] ?: HomeSettings().showBigDate,
                showScreenTimeWithAppName = prefs[Keys.SHOW_SCREEN_TIME_WITH_APP_NAME] ?: HomeSettings().showScreenTimeWithAppName,
                homeAlignment = prefs[Keys.HOME_ALIGNMENT]?.let {
                    HomeAlignment.valueOf(it)
                } ?: HomeSettings().homeAlignment,
                homeVerticalAlignment = prefs[Keys.HOME_VERTICAL_ALIGNMENT]?.let {
                    HomeVerticalAlignment.valueOf(it)
                } ?: HomeSettings().homeVerticalAlignment,
                showFirstTimeHelp = prefs[Keys.SHOW_FIRST_TIME_HELP] ?: HomeSettings().showFirstTimeHelp
            )

            val updated = current.update()
            prefs[Keys.SHOW_SCREEN_TIME_PAGE] = updated.showScreenTimePage
            prefs[Keys.SHOW_CLOCK] = updated.showClock
            prefs[Keys.SHOW_BIG_CLOCK] = updated.showBigClock
            prefs[Keys.USE_TWELVE_HOUR_DISPLAY] = updated.useTwelveHourDisplay
            prefs[Keys.SHOW_DATE] = updated.showDate
            prefs[Keys.SHOW_BIG_DATE] = updated.showBigDate
            prefs[Keys.SHOW_SCREEN_TIME_WITH_APP_NAME] = updated.showScreenTimeWithAppName
            prefs[Keys.HOME_ALIGNMENT] = updated.homeAlignment.name
            prefs[Keys.HOME_VERTICAL_ALIGNMENT] = updated.homeVerticalAlignment.name
            prefs[Keys.SHOW_FIRST_TIME_HELP] = updated.showFirstTimeHelp
        }
    }

    override suspend fun updateAppListSettings(update: AppListSettings.() -> AppListSettings) {
        dataStore.edit { prefs ->
            val current = AppListSettings(
                showSearchBox = prefs[Keys.SHOW_SEARCH_BOX] ?: AppListSettings().showSearchBox,
                showSearchBoxAtBottom = prefs[Keys.SHOW_SEARCH_BOX_AT_BOTTOM] ?: AppListSettings().showSearchBoxAtBottom,
                appsListAlignment = prefs[Keys.APPS_ALIGNMENT]?.let {
                    AppAlignment.valueOf(it)
                } ?: AppListSettings().appsListAlignment,
                autoFocusSearch = prefs[Keys.AUTO_FOCUS_SEARCH] ?: AppListSettings().autoFocusSearch
            )

            val updated = current.update()
            prefs[Keys.SHOW_SEARCH_BOX] = updated.showSearchBox
            prefs[Keys.SHOW_SEARCH_BOX_AT_BOTTOM] = updated.showSearchBoxAtBottom
            prefs[Keys.APPS_ALIGNMENT] = updated.appsListAlignment.name
            prefs[Keys.AUTO_FOCUS_SEARCH] = updated.autoFocusSearch
        }
    }

    override suspend fun updateSearchSettings(update: SearchSettings.() -> SearchSettings) {
        dataStore.edit { prefs ->
            val current = SearchSettings(
                showHiddenAppsInSearch = prefs[Keys.SHOW_HIDDEN_APPS_IN_SEARCH] ?: SearchSettings().showHiddenAppsInSearch,
                favouriteBoostInSearch = prefs[Keys.FAVOURITE_BOOST_IN_SEARCH] ?: SearchSettings().favouriteBoostInSearch,
                autoOpenOnSearch = prefs[Keys.AUTO_OPEN_ON_SEARCH] ?: SearchSettings().autoOpenOnSearch
            )

            val updated = current.update()
            prefs[Keys.SHOW_HIDDEN_APPS_IN_SEARCH] = updated.showHiddenAppsInSearch
            prefs[Keys.FAVOURITE_BOOST_IN_SEARCH] = updated.favouriteBoostInSearch
            prefs[Keys.AUTO_OPEN_ON_SEARCH] = updated.autoOpenOnSearch
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
