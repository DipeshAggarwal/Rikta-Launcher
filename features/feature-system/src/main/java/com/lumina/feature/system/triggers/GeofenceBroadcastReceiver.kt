package com.lumina.feature.system.triggers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.lumina.core.android.di.ApplicationScope
import com.lumina.core.logging.Logger
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope

@AndroidEntryPoint
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    @Inject @ApplicationScope lateinit var scope: CoroutineScope

    @Inject lateinit var triggerSystemStateCache: TriggerSystemStateCache
    @Inject lateinit var logger: Logger

    private val TAG = this::class.java.simpleName

    override fun onReceive(p0: Context?, p1: Intent?) {
        val geofencingEvent = p1?.let { GeofencingEvent.fromIntent(it) } ?: return

        if (geofencingEvent.hasError()) {
            logger.e(TAG, "Geofencing error code: ${geofencingEvent.errorCode}")
            return
        }
        val triggerIds = geofencingEvent.triggeringGeofences?.map { it.requestId } ?: return
        when (geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                triggerIds.forEach { triggerSystemStateCache.addActiveGeofence(it) }
                logger.d(TAG, "Geofence Enter: ${triggerIds.joinToString()}.")
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                triggerIds.forEach { triggerSystemStateCache.removeActiveGeofence(it) }
                logger.d(TAG, "Geofence Exit: ${triggerIds.joinToString()}.")
            }
            else -> {
                logger.w(TAG, "Unknown Geofence transition: ${geofencingEvent.geofenceTransition}.")
            }
        }
    }
}
