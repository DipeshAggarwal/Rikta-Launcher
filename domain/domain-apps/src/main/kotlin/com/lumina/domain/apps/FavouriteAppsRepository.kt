package com.lumina.domain.apps

import kotlinx.coroutines.flow.Flow

/**
 * Contract for managing the persistent state of the user's favorite apps.
 * Unlike HiddenApps, the order of this list is significant for UI presentation.
 */
interface FavouriteAppsRepository {
    val favouriteAppPackages: Flow<List<String>>

    suspend fun addFavouriteApp(packageName: String)
    suspend fun removeFavouriteApp(packageName: String)
    suspend fun setFavouriteApps(packageNames: List<String>)
    suspend fun reorderFavouriteApps(fromIndex: Int, toIndex: Int)
}
