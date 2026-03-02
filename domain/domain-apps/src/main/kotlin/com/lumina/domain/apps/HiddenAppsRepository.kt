package com.lumina.domain.apps

import kotlinx.coroutines.flow.Flow

/**
 * Contract for managing the persistent state of apps the user has chosen to hide from the drawer.
 */
interface HiddenAppsRepository {
    val appPackages: Flow<Set<String>>

    suspend fun addApp(packageName: String)
    suspend fun removeApp(packageName: String)
    suspend fun setApps(packageNames: List<String>)
}
