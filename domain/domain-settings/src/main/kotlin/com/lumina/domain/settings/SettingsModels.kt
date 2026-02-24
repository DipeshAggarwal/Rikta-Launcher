package com.lumina.domain.settings

import com.lumina.core.common.AppDefaults.DEFAULT_SPACER_HEIGHT

data class HomeSettings(
    val showScreenTimePage: Boolean = false,
    val showClock: Boolean = true,
    val showBigClock: Boolean = true,
    val useTwelveHourDisplay: Boolean = false,
    val showDate: Boolean = true,
    val showBigDate: Boolean = true,
    val showScreenTimeWithAppName: Boolean = false,
    val homeAlignment: HomeAlignment = HomeAlignment.Start,
    val homeVerticalAlignment: HomeVerticalAlignment = HomeVerticalAlignment.Top,
    val showFirstTimeHelp: Boolean = true
)
data class AppListSettings(
    val showSearchBox: Boolean = true,
    val showSearchBoxAtBottom: Boolean = true,
    val appsListAlignment: AppAlignment = AppAlignment.Start,
    val autoFocusSearch: Boolean = true
)

data class SearchSettings(
    val showHiddenAppsInSearch: Boolean = true,
    val favouriteBoostInSearch: Boolean = true,
    val autoOpenOnSearch: Boolean = true
)

data class LayoutSettings(
    val spacerHeight: Int = DEFAULT_SPACER_HEIGHT,
    val isImmersiveMode: Boolean = false
)

data class LauncherSettings(
    val home: HomeSettings = HomeSettings(),
    val appList: AppListSettings = AppListSettings(),
    val search: SearchSettings = SearchSettings(),
    val layout: LayoutSettings = LayoutSettings()
)
