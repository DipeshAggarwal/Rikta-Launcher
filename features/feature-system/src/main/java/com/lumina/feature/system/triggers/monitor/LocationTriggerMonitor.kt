package com.lumina.feature.system.triggers.monitor

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.lumina.core.logging.Logger
import com.lumina.core.model.ProfileTriggerType
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.TriggerCondition
import com.lumina.feature.system.triggers.GeofenceBroadcastReceiver
import com.lumina.feature.system.triggers.TriggerMonitor
import com.lumina.feature.system.triggers.TriggerSystemStateCache
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.first

private const val LOCATION_REQUEST_CODE = 2048

@Singleton
class LocationTriggerMonitor @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val triggerSystemStateCache: TriggerSystemStateCache,
    private val profileRepository: ProfileRepository,
    private val logger: Logger
) : TriggerMonitor {
    private val TAG = this::class.java.simpleName
    private val geofencingClient = LocationServices.getGeofencingClient(context)

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    suspend fun registerGeofences() {
        val profiles = profileRepository.getAllProfiles().first()
        val geofences = mutableListOf<Geofence>()

        profiles.forEach { profile ->
            profileRepository.getProfileTriggers(profile.id).first()
                .filter { it.triggerType == ProfileTriggerType.LOCATION }
                .forEach { trigger ->
                    val lat = trigger.latitude ?: return@forEach
                    val lng = trigger.longitude ?: return@forEach
                    val radius = trigger.radiusMeters ?: return@forEach

                    geofences.add(
                        Geofence.Builder()
                            .setRequestId("${profile.id}::${trigger.triggerId}")
                            .setCircularRegion(lat, lng, radius)
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(
                                Geofence.GEOFENCE_TRANSITION_ENTER or
                                Geofence.GEOFENCE_TRANSITION_EXIT
                            )
                            .build()
                    )
                }
        }

        if (geofences.isEmpty()) {
            logger.d(TAG, "No location triggers. Skipping geofence registration.")
            return
        }

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()

        geofencingClient.addGeofences(request, getPendingIntent())
            .addOnSuccessListener {
                logger.d(TAG, "Registered ${geofences.size} geofence.")
            }
            .addOnFailureListener { e ->
                logger.e(TAG, "Failed to register geofence.", e)
            }
    }

    fun unregisterGeofences() {
        geofencingClient.removeGeofences(getPendingIntent())
            .addOnSuccessListener {
                logger.d(TAG, "Geofence removed.")
            }
            .addOnFailureListener { e ->
                logger.e(TAG, "Failed to remove geofences.", e)
            }
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            LOCATION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun evaluate(trigger: TriggerCondition): Boolean {
        val requestId = "${trigger.profileId}::${trigger.triggerId}"
        return triggerSystemStateCache.activeGeofenceIds.value.contains(requestId)
    }
}
