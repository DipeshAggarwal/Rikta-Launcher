package com.lumina.feature.system.triggers

import com.lumina.core.android.di.ApplicationScope
import com.lumina.core.logging.Logger
import com.lumina.domain.coordination.TriggerScheduler
import com.lumina.feature.system.triggers.monitor.BluetoothTriggerMonitor
import com.lumina.feature.system.triggers.monitor.LocationTriggerMonitor
import com.lumina.feature.system.triggers.monitor.TimeTriggerScheduler
import com.lumina.feature.system.triggers.monitor.WifiTriggerMonitor
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Singleton
class TriggerLifecycleManager @Inject constructor(
    @param:ApplicationScope private val scope: CoroutineScope,
    private val triggerEvaluationEngine: TriggerEvaluationEngine,
    private val timeTriggerScheduler: TimeTriggerScheduler,
    private val wifiTriggerMonitor: WifiTriggerMonitor,
    private val bluetoothTriggerMonitor: BluetoothTriggerMonitor,
    private val locationTriggerMonitor: LocationTriggerMonitor,
    private val logger: Logger
) : TriggerScheduler {
    private val TAG = this::class.java.simpleName

    // Ensure Manager is not started multiple times.
    @Volatile private var started = false

    fun start() {
        if (started) {
            logger.d(TAG, "Trigger Lifecycle is already running.")
            return
        }
        logger.d(TAG, "Starting Trigger Lifecycle.")

        triggerEvaluationEngine.start()
        scope.launch {
            timeTriggerScheduler.scheduleNextAlarm()
        }

        try {
            wifiTriggerMonitor.register()
        } catch (e: SecurityException) {
            logger.w(TAG, "Failed to register Wifi monitor.", e)
        }

        try {
            bluetoothTriggerMonitor.register()
        } catch (e: SecurityException) {
            logger.w(TAG, "Failed to register Bluetooth monitor", e)
        }

        scope.launch {
            try {
                locationTriggerMonitor.registerGeofences()
            } catch (e: SecurityException) {
                logger.w(TAG, "Failed to register Location monitor.", e)
            }
        }
        started = true
    }

    fun stop() {
        logger.d(TAG, "Stopping Trigger Lifecycle.")

        triggerEvaluationEngine.stop()

        try {
            wifiTriggerMonitor.unregister()
        } catch (e: Exception) {
            logger.w(TAG, "Exception unregistering Wifi monitor.", e)
        }

        try {
            bluetoothTriggerMonitor.unregister()
        } catch (e: Exception) {
            logger.w(TAG, "Exception unregistering Bluetooth monitor.", e)
        }

        try {
            locationTriggerMonitor.unregisterGeofences()
        } catch (e: Exception) {
            logger.w(TAG, "Exception unregistering location monitor.", e)
        }

        try {
            timeTriggerScheduler.cancel()
        } catch (e: Exception) {
            logger.w(TAG, "Exception cancelling Time scheduler.", e)
        }
        started = false
    }

    override suspend fun refresh() {
        timeTriggerScheduler.scheduleNextAlarm()
        try {
            locationTriggerMonitor.unregisterGeofences()
            locationTriggerMonitor.registerGeofences()
        } catch (e: SecurityException) {
            logger.w(TAG, "Location permission not granted. Cannot refresh geofence.", e)
        }
    }
}
