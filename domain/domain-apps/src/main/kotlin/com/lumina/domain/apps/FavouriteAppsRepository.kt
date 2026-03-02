package com.lumina.domain.apps

import kotlinx.coroutines.flow.Flow

/**
 * Contract for managing the persistent state of the user's favorite apps.
 * Unlike HiddenApps, the order of this list is significant for UI presentation.
 */
interface FavouriteAppsRepository {
    val appPackages: Flow<List<String>>

    suspend fun addApp(packageName: String)
    suspend fun removeApp(packageName: String)
    suspend fun setApps(packageNames: List<String>)
    suspend fun reorderApps(fromIndex: Int, toIndex: Int)
}
