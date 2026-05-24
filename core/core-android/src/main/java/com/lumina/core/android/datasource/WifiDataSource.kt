package com.lumina.core.android.datasource

import android.content.Context
import android.net.wifi.WifiManager
import com.lumina.core.android.PlatformCapabilityChecker
import com.lumina.core.logging.Logger
import com.lumina.domain.coordination.TriggerSystemStateCache
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

data class WifiNetworkInfo(val ssid: String, val isConnected: Boolean)

@Singleton
class WifiDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val capabilityChecker: PlatformCapabilityChecker,
    private val triggerSystemStateCache: TriggerSystemStateCache,
    private val logger: Logger
) {
    private val TAG = this::class.java.simpleName
    private val wifiManager = context.applicationContext
        .getSystemService(Context.WIFI_SERVICE) as? WifiManager

    fun getConnectedNetwork(): List<WifiNetworkInfo> {
        val ssid = triggerSystemStateCache.currentSsid.value ?: return emptyList()
        return listOf(WifiNetworkInfo(ssid = ssid, isConnected = true))
    }

    @Suppress("DEPRECATION", "MissingPermission")
    fun getAvailableNetworks(): List<WifiNetworkInfo> {
        val connectedSsid = triggerSystemStateCache.currentSsid.value
        val connected = connectedSsid?.let {
            listOf(WifiNetworkInfo(ssid = it, isConnected = true))
        }.orEmpty()

        if (!capabilityChecker.canMonitorWifiTriggers()) {
            logger.d(TAG, "Wifi related permissions are not given.")
            return emptyList()
        }
        val scanResults = wifiManager?.scanResults.orEmpty()
            .asSequence()
            .mapNotNull { it.SSID?.takeIf { ssid -> ssid.isNotBlank() } }
            .distinct()
            .filter { it != connectedSsid}
            .sorted()
            .map { ssid -> WifiNetworkInfo(ssid = ssid, isConnected = ssid == connectedSsid) }
            .toList()

        return connected + scanResults
    }
}
