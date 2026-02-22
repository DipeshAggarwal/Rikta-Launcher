package com.lumina.domain.settings

import com.lumina.core.common.AppDefaults.DEFAULT_SPACER_HEIGHT

data class HomeSettings(
    val showScreenTimePage: Boolean = false,
    val showBigClock: Boolean = false,
    val showScreenTimeWithAppName: Boolean = false
)
data class AppListSettings(
    val showSearchBox: Boolean = false,
    val showSearchBoxAtBottom: Boolean = false,
    val appsListAlignment:  AppsAlignment = AppsAlignment.Start
)

data class SearchSettings(
    val showHiddenAppsInSearch: Boolean = false,
    val favouriteBoostInSearch: Boolean = false
)

data class LayoutSettings(
    val spacerHeight: Int = DEFAULT_SPACER_HEIGHT
)

data class LauncherSettings(
    val home: HomeSettings = HomeSettings(),
    val appList: AppListSettings = AppListSettings(),
    val search: SearchSettings = SearchSettings(),
    val layout: LayoutSettings = LayoutSettings()
)
