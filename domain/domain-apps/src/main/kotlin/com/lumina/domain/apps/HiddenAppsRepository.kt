package com.lumina.domain.apps

import kotlinx.coroutines.flow.Flow

/**
 * Contract for managing the persistent state of apps the user has chosen to hide from the drawer.
 */
interface HiddenAppsRepository {
    val hiddenAppPackages: Flow<Set<String>>

    suspend fun addHiddenApp(packageName: String)
    suspend fun removeHiddenApp(packageName: String)
    suspend fun setHiddenApps(packageNames: List<String>)
}
