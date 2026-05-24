package com.lumina.core.android.datasource

import android.content.Context
import android.location.LocationManager
import com.lumina.core.android.PlatformCapabilityChecker
import com.lumina.core.logging.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

data class LocationInfo(val latitude: Double, val longitude: Double)

@Singleton
class LocationDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val capabilityChecker: PlatformCapabilityChecker,
    private val logger: Logger
) {
    private val TAG = this::class.java.simpleName

    @Suppress("MissingPermission")
    fun getLastKnownLocation(): LocationInfo? {
        if (!capabilityChecker.canMonitorLocationTriggers()) {
            logger.d(TAG, "Location related permissions are not given.")
            return null
        }

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        return try {
            val location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: return null
            LocationInfo(latitude = location.latitude, longitude = location.longitude)
        } catch (e: SecurityException) {
            logger.w(TAG, "Location permission revoked.")
            null
        }
    }
}
