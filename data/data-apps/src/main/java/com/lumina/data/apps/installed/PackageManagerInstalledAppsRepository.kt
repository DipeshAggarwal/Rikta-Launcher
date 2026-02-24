package com.lumina.data.apps.installed

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.UserHandle
import android.os.UserManager
import com.lumina.core.common.IoDispatcher
import com.lumina.core.logging.Logger
import com.lumina.domain.apps.AppInfo
import com.lumina.domain.apps.InstalledAppsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.util.concurrent.ConcurrentHashMap

/**
 * Android implementation of [InstalledAppsRepository] using [PackageManager].
 * Uses a local cache to avoid expensive IPC calls to the system on every refresh.
 */
class PackageManagerInstalledAppsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val installedAppsMonitor: InstalledAppsMonitor,
    private val logger: Logger
): InstalledAppsRepository {
    private val TAG = this::class.java.simpleName

    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val userManager = context.getSystemService(UserManager::class.java)

    // Per-Profile Cache.
    // Querying the system for labels is an Inter-Process Communication (IPC) call and is quite expensive.
    private val appCache = ConcurrentHashMap<UserHandle, List<AppInfo>>()

    override suspend fun getDisplayName(packageName: String): String? = withContext(ioDispatcher) {
        try {
            appCache.values
                .flatten()
                .firstOrNull { it.packageName == packageName }
                ?.displayName
        } catch (e: PackageManager.NameNotFoundException) {
            logger.w(TAG, "App not found: $packageName", e)
            throw e
        }
    }

    /**
     * Provides a reactive stream of installed apps with launcher activities.
     * Re-queries the system whenever [installedAppsMonitor] emits a change.
     */
    override fun installedApps(): Flow<List<AppInfo>> =
        installedAppsMonitor.appChanges()
            .onStart { emit(AppChangeEvent.Initial) }
            .map { event ->
                invalidateCache(event)
                loadInstalledApps()
            }

    private suspend fun loadInstalledApps(): List<AppInfo> = withContext(ioDispatcher) {
        val profiles = userManager.userProfiles
        val allCache = profiles.all { appCache.containsKey(it) }

        if (allCache) {
            return@withContext appCache.values
                .flatten()
                .distinctBy { Triple(it.packageName, it.componentClassName, it.userHandleNumber) }
                .sortedBy { it.displayName.lowercase() }
        }

        for (profile in profiles) {
            if (appCache.containsKey(profile)) continue

            try {
                val activities = launcherApps.getActivityList(null, profile)
                val apps = activities
                    .filter { it.applicationInfo.packageName != context.packageName }
                    .map { info ->
                        AppInfo(
                            packageName = info.applicationInfo.packageName,
                            displayName = info.label.toString(),
                            componentClassName = info.componentName.className,
                            userHandleNumber = userManager.getSerialNumberForUser(profile)
                        )
                    }
                appCache[profile] = apps
            } catch (e: Exception) {
                logger.e(TAG, "Failed to load apps for profile $profile", e)
                appCache[profile] = emptyList()
            }
        }

        appCache.values
            .flatten()
            .distinctBy { Triple(it.packageName, it.componentClassName, it.userHandleNumber) }
            .sortedBy { it.displayName.lowercase() }
    }

    private fun invalidateCache(event: AppChangeEvent) {
        when (event) {
            is AppChangeEvent.Initial -> appCache.clear()
            is AppChangeEvent.PackageAdded -> updateSinglePackage(event.packageName, event.userHandle)
            is AppChangeEvent.PackageChanged -> updateSinglePackage(event.packageName, event.userHandle)
            is AppChangeEvent.PackageRemoved -> {
                appCache[event.userHandle] = appCache[event.userHandle]
                    ?.filterNot { it.packageName == event.packageName }
                    ?: emptyList()
            }
            is AppChangeEvent.PackagesAvailable,
            is AppChangeEvent.PackagesUnavailable -> {
                appCache.remove(event.userHandle)
            }
        }
    }

    private fun updateSinglePackage(packageName: String, userHandle: UserHandle) {
        if (packageName == context.packageName) return

        try {
            val activities = launcherApps.getActivityList(packageName, userHandle)
            val updatedAppsInfo = activities.map { info ->
                AppInfo(
                    packageName = info.applicationInfo.packageName,
                    displayName = info.label.toString(),
                    componentClassName = info.componentName.className,
                    userHandleNumber = userManager.getSerialNumberForUser(userHandle)
                )
            }

            val currentList = appCache[userHandle] ?: emptyList()
            val oldList = currentList.filterNot { it.packageName == packageName }

            appCache[userHandle] = oldList + updatedAppsInfo
        } catch (e: Exception) {
            logger.e("$TAG:UpdateSingle", "Failed to update cache for $packageName", e)
        }
    }
}
