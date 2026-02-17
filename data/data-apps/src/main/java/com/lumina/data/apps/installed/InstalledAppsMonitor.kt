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
import kotlinx.coroutines.channels.awaitClose

/**
 * Monitors the system for app installations, uninstalls, and updates.
 * Uses [LauncherApps.Callback] which is more efficient and reliable for launcher-specific app
 * monitoring than broad BroadcastReceivers.
 */
class InstalledAppsMonitor @Inject constructor(
    @param:ApplicationContext private val context: Context
){
    fun appChanges(): Flow<Unit> = callbackFlow {
        val launcherApps = context.getSystemService(LauncherApps::class.java)

        val callback = object : LauncherApps.Callback() {
            // All overrides trigger a Unit emission to notify consumers to refresh data
            override fun onPackageAdded(packageName: String?, user: UserHandle?) {
                trySend(Unit)
            }

            override fun onPackageChanged(packageName: String?, user: UserHandle?) {
                trySend(Unit)
            }

            override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
                trySend(Unit)
            }

            override fun onPackagesAvailable(
                packageNames: Array<out String?>?,
                user: UserHandle?,
                replacing: Boolean
            ) {
                trySend(Unit)
            }

            override fun onPackagesUnavailable(
                packageNames: Array<out String?>?,
                user: UserHandle?,
                replacing: Boolean
            ) {
                trySend(Unit)
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
