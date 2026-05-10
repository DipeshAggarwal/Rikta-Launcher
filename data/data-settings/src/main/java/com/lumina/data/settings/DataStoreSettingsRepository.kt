package com.lumina.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lumina.core.datastore.di.SettingsDataStore
import com.lumina.domain.settings.AppListSettings
import com.lumina.domain.settings.AppAlignment
import com.lumina.domain.settings.CountdownSettings
import com.lumina.domain.settings.HomeAlignment
import com.lumina.domain.settings.HomeSettings
import com.lumina.domain.settings.HomeVerticalAlignment
import com.lumina.domain.settings.LauncherSettings
import com.lumina.domain.settings.LayoutSettings
import com.lumina.domain.settings.SearchSettings
import com.lumina.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject
import kotlinx.coroutines.flow.combine

class DataStoreSettingsRepository @Inject constructor(
    @param:SettingsDataStore private val dataStore: DataStore<Preferences>
) : SettingsRepository {
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

        // COUNTDOWN
        val COUNTDOWN_DURATION_PER_STEP = intPreferencesKey("countdown_duration_per_step")
        val COUNTDOWN_STEPS = intPreferencesKey("countdown_steps")
        val COUNTDOWN_WRAP_DURATION = longPreferencesKey("countdown_wrap_duration")
        val COUNTDOWN_SHOW_TEXT = booleanPreferencesKey("countdown_show_text")

        // Layout
        val SPACER_HEIGHT = intPreferencesKey("spacer_height")
    }

    private val defaultHomeSettings = HomeSettings()
    private val defaultAppListSettings = AppListSettings()
    private val defaultSearchSettings = SearchSettings()
    private val defaultCountdownSettings = CountdownSettings()
    private val defaultLayoutSettings = LayoutSettings()

    private fun parseHomeSettings(prefs: Preferences): HomeSettings {
        return HomeSettings(
            showScreenTimePage = prefs[Keys.SHOW_SCREEN_TIME_PAGE] ?: defaultHomeSettings.showScreenTimePage,
            showClock = prefs[Keys.SHOW_CLOCK] ?: defaultHomeSettings.showClock,
            showBigClock = prefs[Keys.SHOW_BIG_CLOCK] ?: defaultHomeSettings.showBigClock,
            useTwelveHourDisplay = prefs[Keys.USE_TWELVE_HOUR_DISPLAY] ?: defaultHomeSettings.useTwelveHourDisplay,
            showDate = prefs[Keys.SHOW_DATE] ?: defaultHomeSettings.showDate,
            showBigDate = prefs[Keys.SHOW_BIG_DATE] ?: defaultHomeSettings.showBigDate,
            showScreenTimeWithAppName = prefs[Keys.SHOW_SCREEN_TIME_WITH_APP_NAME] ?: defaultHomeSettings.showScreenTimeWithAppName,
            homeAlignment = prefs[Keys.HOME_ALIGNMENT]?.let {
                HomeAlignment.valueOf(it)
            } ?: defaultHomeSettings.homeAlignment,
            homeVerticalAlignment = prefs[Keys.HOME_VERTICAL_ALIGNMENT]?.let {
                HomeVerticalAlignment.valueOf(it)
            } ?: defaultHomeSettings.homeVerticalAlignment,
            showFirstTimeHelp = prefs[Keys.SHOW_FIRST_TIME_HELP] ?: defaultHomeSettings.showFirstTimeHelp
        )
    }

    private fun parseAppListSettings(prefs: Preferences): AppListSettings {
        return AppListSettings(
            showSearchBox = prefs[Keys.SHOW_SEARCH_BOX] ?: defaultAppListSettings.showSearchBox,
            showSearchBoxAtBottom = prefs[Keys.SHOW_SEARCH_BOX_AT_BOTTOM] ?: defaultAppListSettings.showSearchBoxAtBottom,
            appsListAlignment = prefs[Keys.APPS_ALIGNMENT]?.let {
                AppAlignment.valueOf(it)
            } ?: defaultAppListSettings.appsListAlignment,
            autoFocusSearch = prefs[Keys.AUTO_FOCUS_SEARCH] ?: defaultAppListSettings.autoFocusSearch
        )
    }

    private fun parseSearchSettings(prefs: Preferences): SearchSettings {
        return SearchSettings(
            showHiddenAppsInSearch = prefs[Keys.SHOW_HIDDEN_APPS_IN_SEARCH] ?: defaultSearchSettings.showHiddenAppsInSearch,
            favouriteBoostInSearch = prefs[Keys.FAVOURITE_BOOST_IN_SEARCH] ?: defaultSearchSettings.favouriteBoostInSearch,
            autoOpenOnSearch = prefs[Keys.AUTO_OPEN_ON_SEARCH] ?: defaultSearchSettings.autoOpenOnSearch,
        )
    }

    private fun parseCountdownSettings(prefs: Preferences): CountdownSettings {
        return CountdownSettings(
            countdownDurationPerStep = prefs[Keys.COUNTDOWN_DURATION_PER_STEP] ?: defaultCountdownSettings.countdownDurationPerStep,
            countdownSteps = prefs[Keys.COUNTDOWN_STEPS] ?: defaultCountdownSettings.countdownSteps,
            countdownWrapDuration = prefs[Keys.COUNTDOWN_WRAP_DURATION] ?: defaultCountdownSettings.countdownWrapDuration,
            showText = prefs[Keys.COUNTDOWN_SHOW_TEXT] ?: defaultCountdownSettings.showText
        )
    }

    private fun parseLayoutSettings(prefs: Preferences): LayoutSettings {
        return LayoutSettings(
            spacerHeight = prefs[Keys.SPACER_HEIGHT] ?: defaultLayoutSettings.spacerHeight
        )
    }

    override val homeSettings: Flow<HomeSettings> = dataStore.data.map { parseHomeSettings(it) }
    override val appListSettings: Flow<AppListSettings> = dataStore.data.map { parseAppListSettings(it) }
    override val searchSettings: Flow<SearchSettings> = dataStore.data.map { parseSearchSettings(it) }
    override val countdownSettings: Flow<CountdownSettings> = dataStore.data.map { parseCountdownSettings(it) }
    override val layoutSettings: Flow<LayoutSettings> = dataStore.data.map { parseLayoutSettings(it) }
    override val allSettings: Flow<LauncherSettings> = dataStore.data.map { prefs ->
        LauncherSettings(
            home = parseHomeSettings(prefs),
            appList = parseAppListSettings(prefs),
            search = parseSearchSettings(prefs),
            countdown = parseCountdownSettings(prefs),
            layout = parseLayoutSettings(prefs),
        )
    }

    override suspend fun updateHomeSettings(update: HomeSettings.() -> HomeSettings) {
        dataStore.edit { prefs ->
            val current = parseHomeSettings(prefs)
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
            val current = parseAppListSettings(prefs)
            val updated = current.update()

            prefs[Keys.SHOW_SEARCH_BOX] = updated.showSearchBox
            prefs[Keys.SHOW_SEARCH_BOX_AT_BOTTOM] = updated.showSearchBoxAtBottom
            prefs[Keys.APPS_ALIGNMENT] = updated.appsListAlignment.name
            prefs[Keys.AUTO_FOCUS_SEARCH] = updated.autoFocusSearch
        }
    }

    override suspend fun updateSearchSettings(update: SearchSettings.() -> SearchSettings) {
        dataStore.edit { prefs ->
            val current = parseSearchSettings(prefs)
            val updated = current.update()

            prefs[Keys.SHOW_HIDDEN_APPS_IN_SEARCH] = updated.showHiddenAppsInSearch
            prefs[Keys.FAVOURITE_BOOST_IN_SEARCH] = updated.favouriteBoostInSearch
            prefs[Keys.AUTO_OPEN_ON_SEARCH] = updated.autoOpenOnSearch
        }
    }

    override suspend fun updateCountdownSettings(update: CountdownSettings.() -> CountdownSettings) {
        dataStore.edit { prefs ->
            val current = parseCountdownSettings(prefs)
            val updated = current.update()

            prefs[Keys.COUNTDOWN_DURATION_PER_STEP] = updated.countdownDurationPerStep
            prefs[Keys.COUNTDOWN_STEPS] = updated.countdownSteps
            prefs[Keys.COUNTDOWN_WRAP_DURATION] = updated.countdownWrapDuration
            prefs[Keys.COUNTDOWN_SHOW_TEXT] = updated.showText
        }
    }

    override suspend fun updateLayoutSettings(update: LayoutSettings.() -> LayoutSettings) {
        dataStore.edit { prefs ->
            val current = parseLayoutSettings(prefs)
            val updated = current.update()

            prefs[Keys.SPACER_HEIGHT] = updated.spacerHeight
        }
    }
}
