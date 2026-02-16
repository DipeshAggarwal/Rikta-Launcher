package com.lumina.domain.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun setShowHiddenAppsInSearch(enabled: Boolean)
    fun showHiddenAppsInSearch(): Flow<Boolean>
}
