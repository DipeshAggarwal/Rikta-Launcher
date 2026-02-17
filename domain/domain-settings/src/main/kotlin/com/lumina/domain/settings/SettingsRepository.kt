package com.lumina.domain.settings

import kotlinx.coroutines.flow.Flow

/**
 * Contract for managing global user preferences and launcher configuration.
 * This repository abstracts the underlying storage mechanism.
 */
interface SettingsRepository {
    /**
     * Updates the preference for hidden app visibility in search.
     */
    suspend fun setShowHiddenAppsInSearch(enabled: Boolean)

    /**
     * Returns a reactive stream indicating whether hidden apps should be visible to the user.
     */
    fun showHiddenAppsInSearch(): Flow<Boolean>
}
