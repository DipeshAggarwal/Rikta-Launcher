package com.lumina.domain.apps

import kotlinx.coroutines.flow.Flow

/**
 * Contract for retrieving information about apps installed on the Android device.
 */
interface InstalledAppsRepository {
    /**
     * Resolves the human-readable label for a specific package.
     * @throws NameNotFoundException if the package is not found.
     */
    suspend fun getDisplayName(packageName: String): String

    /**
     * Returns a reactive stream containing all apps that can be launched by the user.
     * Usually filters for apps with [Intent.CATEGORY_LAUNCHER].
     */
    fun installedApps(): Flow<List<AppInfo>>
}
