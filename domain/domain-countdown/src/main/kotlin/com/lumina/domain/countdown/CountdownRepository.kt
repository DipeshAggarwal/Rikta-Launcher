package com.lumina.domain.countdown

import kotlinx.coroutines.flow.Flow

interface CountdownRepository {
    val countdownAppPackages: Flow<Set<String>>

    suspend fun addCountdownApp(packageName: String)
    suspend fun removeCountdownApp(packageName: String)
}
