package com.lumina.data.apps.installed

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.UserHandle
import android.os.UserManager
import com.lumina.core.common.IoDispatcher
import com.lumina.core.android.di.ApplicationScope
import com.lumina.core.database.dao.AppOverrideDao
import com.lumina.core.logging.Logger
import com.lumina.core.model.AppCategory
import com.lumina.core.model.AppInfo
import com.lumina.core.model.componentKey
import com.lumina.domain.apps.InstalledAppsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

// Note: Look into swapping out AppInfo to AppOverrideState. Maybe only one model is needed.
// This will simplify the usecase too, which needs the extra data.

/**
 * Android implementation of [InstalledAppsRepository] using [PackageManager].
 * Uses a local cache to avoid expensive IPC calls to the system on every refresh.
 */
@Singleton
class PackageManagerInstalledAppsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:ApplicationScope private val scope: CoroutineScope,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val installedAppsMonitor: InstalledAppsMonitor,
    private val appOverrideDao: AppOverrideDao,
    private val userManager: UserManager,
    private val logger: Logger
) : InstalledAppsRepository {
    private val TAG = this::class.java.simpleName

    private val launcherApps = context.getSystemService(LauncherApps::class.java)

    // Live OS state.
    private val _systemApps = MutableStateFlow<List<AppInfo>>(emptyList())

    init {
        scope.launch {
            installedAppsMonitor.appChanges()
                .onStart { emit(AppChangeEvent.Initial) }
                .collect { event -> handleSystemEvent(event) }
        }
    }

    /**
     * Provides a reactive stream of installed apps with launcher activities.
     * Re-queries the system whenever [installedAppsMonitor] emits a change.
     */
    override val apps: StateFlow<List<AppInfo>> = combine(
        _systemApps, appOverrideDao.getAllOverrides()
    ) { systemApps, appOverrides ->
        val overrideMap = appOverrides.associateBy { "${it.packageName}:${it.userHandleNumber}" }

        systemApps.map { systemApp ->
            val overrideData = overrideMap[systemApp.componentKey]
            val overrideCategory = overrideData?.categoryOverride

            if (overrideData != null) {
                systemApp.copy(
                    displayName = overrideData.customDisplayName ?: systemApp.displayName,
                    category = overrideCategory ?: systemApp.category,
                    customCategoryName = overrideData.customCategoryName
                )
            } else {
                systemApp
            }
        }.sortedBy { it.displayName.lowercase() }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val appsMap: StateFlow<Map<String, AppInfo>> = apps
        .map { list -> list.associateBy { it.componentKey } }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override suspend fun getLabel(packageName: String): String? {
        return apps.value.firstOrNull { it.packageName == packageName }?.displayName
            ?: withContext(ioDispatcher) {
                try {
                    userManager.userProfiles.firstNotNullOfOrNull { profile ->
                        launcherApps
                            .getActivityList(packageName, profile)
                            .firstOrNull()
                            ?.label
                            ?.toString()
                    }
                } catch (e: Exception) {
                    logger.w(TAG, "App not found: $packageName", e)
                    null
                }
            }
    }

    private fun loadAllFromSystem(): List<AppInfo> {
        return userManager.userProfiles.flatMap { profile ->
            try {
                launcherApps.getActivityList(null, profile)
                    .filter { it.applicationInfo.packageName != context.packageName }
                    .map { info ->
                        AppInfo(
                            packageName = info.applicationInfo.packageName,
                            componentClassName = info.componentName.className,
                            userHandleNumber = userManager.getSerialNumberForUser(profile),
                            displayName = info.label.toString(),
                            category = mapCategory(info.applicationInfo.category)
                        )
                    }
            } catch (e: Exception) {
                logger.e(TAG, "Failed to load apps for profile $profile", e)
                emptyList()
            }
        }
            .distinctBy { Triple(it.packageName, it.componentClassName, it.userHandleNumber) }
            .sortedBy { it.displayName.lowercase() }
    }

    private fun loadSingleFromSystem(packageName: String, userHandle: UserHandle): AppInfo? {
        if (packageName == context.packageName) return null

        return try {
            launcherApps.getActivityList(packageName, userHandle)
                .firstOrNull()
                ?.let { info ->
                    AppInfo(
                        packageName = info.applicationInfo.packageName,
                        componentClassName = info.componentName.className,
                        userHandleNumber = userManager.getSerialNumberForUser(userHandle),
                        displayName = info.label.toString(),
                        category = mapCategory(info.applicationInfo.category)
                    )
                }
        } catch (e: Exception) {
            logger.e(TAG, "Failed to load app $packageName.", e)
            null
        }
    }

    private suspend fun handleSystemEvent(event: AppChangeEvent) = withContext(ioDispatcher) {
        val currentAppsList = _systemApps.value.toMutableList()

        when (event) {
            is AppChangeEvent.Initial -> {
                _systemApps.value = loadAllFromSystem()
            }

            is AppChangeEvent.PackageAdded -> {
                val serial = userManager.getSerialNumberForUser(event.userHandle)
                currentAppsList.removeAll { it.packageName == event.packageName && it.userHandleNumber == serial }

                val updated = loadSingleFromSystem(event.packageName, event.userHandle)
                if (updated != null) {
                    currentAppsList.add(updated)
                    _systemApps.value = currentAppsList.sortedBy { it.displayName.lowercase() }
                }
            }

            is AppChangeEvent.PackageChanged -> {
                val serial = userManager.getSerialNumberForUser(event.userHandle)
                currentAppsList.removeAll { it.packageName == event.packageName && it.userHandleNumber == serial }

                val updated = loadSingleFromSystem(event.packageName, event.userHandle)
                if (updated != null) {
                    currentAppsList.add(updated)
                }
                _systemApps.value = currentAppsList.sortedBy { it.displayName.lowercase() }
            }

            is AppChangeEvent.PackageRemoved -> {
                val serial = userManager.getSerialNumberForUser(event.userHandle)
                currentAppsList.removeAll { it.packageName == event.packageName && it.userHandleNumber == serial }

                _systemApps.value = currentAppsList
                appOverrideDao.deleteOverride(event.packageName, serial)
            }

            is AppChangeEvent.PackagesAvailable -> {
                val serial = userManager.getSerialNumberForUser(event.userHandle)
                event.packageNames.forEach { packageName ->
                    currentAppsList.removeAll { it.packageName == packageName && it.userHandleNumber == serial }

                    val updated = loadSingleFromSystem(packageName, event.userHandle)
                    if (updated != null) currentAppsList.add(updated)
                }
                _systemApps.value = currentAppsList.sortedBy { it.displayName.lowercase() }
            }

            is AppChangeEvent.PackagesUnavailable -> {
                val serial = userManager.getSerialNumberForUser(event.userHandle)
                event.packageNames.forEach { packageName ->
                    currentAppsList.removeAll { it.packageName == packageName && it.userHandleNumber == serial }
                    appOverrideDao.deleteOverride(packageName, serial)
                }
                _systemApps.value = currentAppsList
            }
        }
    }

    private fun mapCategory(systemCategory: Int): AppCategory = when (systemCategory) {
        ApplicationInfo.CATEGORY_GAME -> AppCategory.GAME
        ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.SOCIAL
        ApplicationInfo.CATEGORY_IMAGE -> AppCategory.SOCIAL
        ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.PRODUCTIVITY
        ApplicationInfo.CATEGORY_AUDIO -> AppCategory.ENTERTAINMENT
        ApplicationInfo.CATEGORY_VIDEO -> AppCategory.ENTERTAINMENT
        ApplicationInfo.CATEGORY_NEWS -> AppCategory.NEWS
        ApplicationInfo.CATEGORY_MAPS -> AppCategory.TRAVEL
        else -> AppCategory.UNCATEGORISED
    }
}
