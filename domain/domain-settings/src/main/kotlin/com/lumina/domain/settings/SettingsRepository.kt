package com.lumina.domain.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val homeSettings: Flow<HomeSettings>
    val appListSettings: Flow<AppListSettings>
    val searchSettings: Flow<SearchSettings>
    val countdownSettings: Flow<CountdownSettings>
    val layoutSettings: Flow<LayoutSettings>

    suspend fun updateHomeSettings(update: HomeSettings.() -> HomeSettings)
    suspend fun updateAppListSettings(update: AppListSettings.() -> AppListSettings)
    suspend fun updateSearchSettings(update: SearchSettings.() -> SearchSettings)
    suspend fun updateCountdownSettings(update: CountdownSettings.() -> CountdownSettings)
    suspend fun updateLayoutSettings(update: LayoutSettings.() -> LayoutSettings)
}
