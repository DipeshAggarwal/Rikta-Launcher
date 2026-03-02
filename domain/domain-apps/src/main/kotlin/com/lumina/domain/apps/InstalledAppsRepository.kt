package com.lumina.domain.apps

import com.lumina.core.model.AppInfo
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for retrieving information about apps installed on the Android device.
 */
interface InstalledAppsRepository {
    /**
     * Returns a reactive stream containing all apps that can be launched by the user.
     * Usually filters for apps with [Intent.CATEGORY_LAUNCHER].
     */
    val apps: StateFlow<List<AppInfo>>

    /**
     * Resolves the human-readable label for a specific package.
     * @throws NameNotFoundException if the package is not found.
     */
    suspend fun getLabel(packageName: String): String?
}
