package com.lumina.domain.settings

import kotlinx.coroutines.flow.Flow

/**
 * Contract for managing global user preferences and launcher configuration.
 * This repository abstracts the underlying storage mechanism.
 *
 * set* function save the value in DataStore.
 * show* function returns the value as a Flow.
 * reset* function resets the value to Default.
 */
interface SettingsRepository {
    // App Hiding
    suspend fun setShowHiddenAppsInSearch(enabled: Boolean)
    fun showHiddenAppsInSearch(): Flow<Boolean>

    // Hide Screen Time Page
    suspend fun setScreenTimePageInHome(enabled: Boolean)
    fun showScreenTimePageInHome(): Flow<Boolean>

    // Big clock
    suspend fun setBigClockInHome(enabled: Boolean)
    fun showBigClockInHome(): Flow<Boolean>

    // App Usage Time
    suspend fun setScreenTimeVisibleWithApp(enabled: Boolean)
    fun showScreenTimeVisibleWithApp(): Flow <Boolean>

    // Favourite Boost in search
    suspend fun setFavouriteBoostInSearch(enabled: Boolean)
    fun showFavouriteBoostInSearch(): Flow<Boolean>

    // Show Search Box
    suspend fun setSearchBoxInAppList(enabled: Boolean)
    fun showSearchBoxInAppList(): Flow<Boolean>

    // Show Search in Bottom
    suspend fun setSearchBoxAtBottomInAppList(enabled: Boolean)
    fun showSearchBoxAtBottomInAppList(): Flow<Boolean>

    // Spacer Functions
    suspend fun setSpacerHeight(height: Int)
    suspend fun resetSpacerHeight()
    fun spacerHeight(): Flow<Int>
}
