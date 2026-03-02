package com.lumina.domain.countdown

import kotlinx.coroutines.flow.Flow

interface CountdownAppsRepository {
    val appPackages: Flow<Set<String>>

    suspend fun addApp(packageName: String)
    suspend fun removeApp(packageName: String)
    suspend fun setApps(packageNames: List<String>)
}
