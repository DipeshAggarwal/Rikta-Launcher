package com.lumina.feature.system.triggers

import android.Manifest
import androidx.annotation.RequiresPermission
import com.lumina.core.android.di.ApplicationScope
import com.lumina.core.logging.Logger
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
) {
    private val TAG = this::class.java.simpleName

    @RequiresPermission(allOf = [
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_CONNECT
    ]
    )
    fun start() {
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
    }

    fun stop() {
        logger.d(TAG, "Stopping Trigger Lifecycle.")

        triggerEvaluationEngine.stop()

        wifiTriggerMonitor.unregister()
        bluetoothTriggerMonitor.unregister()
        locationTriggerMonitor.unregisterGeofences()
        timeTriggerScheduler.cancel()
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun refresh(scope: CoroutineScope) {
        scope.launch {
            timeTriggerScheduler.scheduleNextAlarm()
            locationTriggerMonitor.unregisterGeofences()
            locationTriggerMonitor.registerGeofences()
        }
    }
}
