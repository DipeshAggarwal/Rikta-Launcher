package com.lumina.data.apps.installed

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
    private val pm = context.packageManager
    private val TAG = this::class.java.simpleName

    // Cache to store app labels (Strings). Querying the system for labels is an Inter-Process
    // Communication (IPC) call and is quite expensive.
    private val labelCache = ConcurrentHashMap<String, String>()

    override suspend fun getDisplayName(packageName: String): String = withContext(ioDispatcher) {
        // Return from memory if available
        labelCache[packageName]?.let { return@withContext it }

        try {
            val appInfo = pm.getApplicationInfo(packageName, PackageManager.MATCH_ALL)
            val label = pm.getApplicationLabel(appInfo).toString()

            labelCache[packageName] = label
            label
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
            .onStart { emit(Unit) }
            .map { loadInstalledApps() }

    private suspend fun loadInstalledApps(): List<AppInfo> = withContext(ioDispatcher) {
        // Only show apps that can appear in the Launcher.
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfo = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        // Use Sequence for lazy evaluation: saves memory by not creating multiple intermediate
        // lists for map/filter/distinct steps.
        resolveInfo
            .asSequence()
            .map { it.activityInfo.packageName }
            .filterNot { it == context.packageName }
            .distinct()
            .mapNotNull { packageName ->
                try {
                    // Reuse cached labels to make refreshes near-instant
                    val label = labelCache.getOrPut(packageName) {
                        val appInfo = pm.getApplicationInfo(
                            packageName,
                            PackageManager.GET_META_DATA
                        )
                        pm.getApplicationLabel(appInfo).toString()
                    }
                    AppInfo(packageName, label)
                } catch (e: PackageManager.NameNotFoundException) {
                    logger.w(TAG, "App not found: $packageName", e)
                    null
                }
            }
            .sortedBy { it.displayName.lowercase() }
            .toList()
    }
}
