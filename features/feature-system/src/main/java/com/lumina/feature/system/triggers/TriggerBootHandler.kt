package com.lumina.feature.system.triggers

import android.Manifest
import androidx.annotation.RequiresPermission
import com.lumina.core.android.di.ApplicationScope
import com.lumina.core.common.BootHandler
import com.lumina.core.logging.Logger
import com.lumina.feature.system.triggers.monitor.LocationTriggerMonitor
import com.lumina.feature.system.triggers.monitor.TimeTriggerScheduler
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Singleton
class TriggerBootHandler @Inject constructor(
    @param:ApplicationScope private val scope: CoroutineScope,
    private val timeTriggerScheduler: TimeTriggerScheduler,
    private val locationTriggerMonitor: LocationTriggerMonitor,
    private val logger: Logger
) : BootHandler {
    private val TAG = this::class.java.simpleName

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onBoot(rebootTime: Long) {
        logger.d(TAG, "Restore triggers after boot.")

        scope.launch {
            timeTriggerScheduler.scheduleNextAlarm()

            try {
                locationTriggerMonitor.registerGeofences()
            } catch (e: SecurityException) {
                logger.w(TAG, "Missing location permissions.")
            }
        }
    }
}
