package com.lumina.feature.system.triggers.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import com.lumina.core.android.PlatformCapabilityChecker
import com.lumina.core.android.di.ApplicationScope
import com.lumina.core.logging.Logger
import com.lumina.domain.profiles.model.TriggerCondition
import com.lumina.feature.system.triggers.TriggerMonitor
import com.lumina.domain.coordination.TriggerSystemStateCache
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val WIFI_RAW_DELIMITER = "\""
private const val WIFI_UNKNOWN_SSID = "<unknown ssid>"

@Singleton
class WifiTriggerMonitor @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:ApplicationScope private val scope: CoroutineScope,
    private val triggerSystemStateCache: TriggerSystemStateCache,
    private val capabilityChecker: PlatformCapabilityChecker,
    private val logger: Logger
) : TriggerMonitor {
    private val TAG = this::class.java.simpleName

    private val connectivityManager = context.getSystemService(
        Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager
    private val wifiManager = context.applicationContext.getSystemService(
        Context.WIFI_SERVICE
    ) as? WifiManager

    private val callback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                wifiChanged(networkCapabilities)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                wifiLost()
            }
        }
    } else {
        object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                wifiChanged(networkCapabilities)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                wifiLost()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun wifiChanged(networkCapabilities: NetworkCapabilities) {
        val rawSsid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (networkCapabilities.transportInfo as? WifiInfo)?.ssid
        } else {
            wifiManager?.connectionInfo?.ssid
        }

        val cleanSsid = rawSsid?.removeSurrounding(WIFI_RAW_DELIMITER)
        if (!cleanSsid.isNullOrBlank() && cleanSsid != WIFI_UNKNOWN_SSID) {
            scope.launch {
                triggerSystemStateCache.updateSsid(cleanSsid)
                logger.d(TAG, "Wifi Changed. Evaluating Triggers.")
            }
        }
    }

    private fun wifiLost() {
        scope.launch {
            triggerSystemStateCache.updateSsid(null)
            logger.d(TAG, "Wifi Disconnected. Evaluating Triggers.")
        }
    }

    // This Suppress is very intentional here as PlatformCapabilityChecker should handle all permission.
    @Suppress("MissingPermission")
    fun register() {
        if (!capabilityChecker.canMonitorWifiTriggers()) {
            logger.d(TAG, "Skipping Wifi registration because of missing permission.")
            return
        }

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        logger.d(TAG, "Wifi Callback registered.")
    }

    fun unregister() {
        try {
            connectivityManager.unregisterNetworkCallback(callback)
            triggerSystemStateCache.updateSsid(null)

            logger.d(TAG, "Wifi Callback unregistered.")
        } catch (e: Exception) {
            logger.w(TAG, "Wifi Callback couldn't unregister.", e)
        }
    }

    override fun evaluate(trigger: TriggerCondition): Boolean {
        val targetSsid = trigger.wifiSsid ?: return false
        return triggerSystemStateCache.currentSsid.value == targetSsid
    }
}