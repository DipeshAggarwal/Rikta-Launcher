package com.lumina.domain.settings

import kotlinx.coroutines.flow.Flow

/**
 * Contract for managing global user preferences and launcher configuration.
 * This repository abstracts the underlying storage mechanism.
 */
interface SettingsRepository {
    // App Hiding Functions
    /**
     * Updates the preference for hidden app visibility in search.
     */
    suspend fun setShowHiddenAppsInSearch(enabled: Boolean)

    /**
     * Returns a reactive stream indicating whether hidden apps should be visible to the user.
     */
    fun showHiddenAppsInSearch(): Flow<Boolean>

    // Spacer Functions
    /**
     * Updates the preference for the spacer height across the App UI.
     */
    suspend fun setSpacerHeight(height: Int)

    /**
     * Resets spacer height across the App UI.
     */
    suspend fun resetSpacerHeight()

    /**
     * Returns a reactive stream which provides the height of the spacer.
     */
    fun spacerHeight(): Flow<Int>
}
