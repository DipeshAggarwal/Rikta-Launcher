package com.lumina.data.apps.installed

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.channels.awaitClose

sealed interface AppChangeEvent {
    data object Initial: AppChangeEvent
    sealed interface ProfileEvent: AppChangeEvent {
        val userHandle: UserHandle
    }

    data class PackageAdded(
        val packageName: String,
        override val userHandle: UserHandle
    ): ProfileEvent

    data class PackageRemoved(
        val packageName: String,
        override val userHandle: UserHandle
    ): ProfileEvent

    data class PackageChanged(
        val packageName: String,
        override val userHandle: UserHandle
    ): ProfileEvent

    data class PackagesAvailable(
        val packageNames: List<String>,
        override val userHandle: UserHandle
    ): ProfileEvent

    data class PackagesUnavailable(
        val packageNames: List<String>,
        override val userHandle: UserHandle
    ): ProfileEvent
}

/**
 * Monitors the system for app installations, uninstalls, and updates.
 * Uses [LauncherApps.Callback] which is more efficient and reliable for launcher-specific app
 * monitoring than broad BroadcastReceivers.
 */
@Singleton
class InstalledAppsMonitor @Inject constructor(
    @param:ApplicationContext private val context: Context
){
    fun appChanges(): Flow<AppChangeEvent> = callbackFlow {
        val launcherApps = context.getSystemService(LauncherApps::class.java)

        val callback = object : LauncherApps.Callback() {
            // All overrides trigger a Unit emission to notify consumers to refresh data
            override fun onPackageAdded(packageName: String, user: UserHandle) {
                trySend(AppChangeEvent.PackageAdded(packageName,user))
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {
                trySend(AppChangeEvent.PackageChanged(packageName, user))
            }

            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                trySend(AppChangeEvent.PackageRemoved(packageName, user))
            }

            override fun onPackagesAvailable(
                packageNames: Array<out String?>,
                user: UserHandle,
                replacing: Boolean
            ) {
                trySend(AppChangeEvent.PackagesAvailable(
                    packageNames.filterNotNull(), user
                ))
            }

            override fun onPackagesUnavailable(
                packageNames: Array<out String?>,
                user: UserHandle,
                replacing: Boolean
            ) {
                trySend(AppChangeEvent.PackagesUnavailable(
                    packageNames.filterNotNull(), user
                ))
            }
        }

        // LauncherApps requires a Handler (main thread)
        val handler = Handler(Looper.getMainLooper())

        // Register the listener with the system
        launcherApps.registerCallback(callback, handler)

        // Cleanup: unregister when the Flow is cancelled.
        awaitClose {
            launcherApps.unregisterCallback(callback)
        }
    }
}
