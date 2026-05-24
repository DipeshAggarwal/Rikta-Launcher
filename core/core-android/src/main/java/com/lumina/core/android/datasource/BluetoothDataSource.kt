package com.lumina.core.android.datasource

import android.bluetooth.BluetoothManager
import android.content.Context
import com.lumina.core.android.PlatformCapabilityChecker
import com.lumina.core.logging.Logger
import com.lumina.domain.coordination.TriggerSystemStateCache
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

data class BluetoothDeviceInfo(val name: String?, val address: String, val isConnected: Boolean)

@Singleton
class BluetoothDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val capabilityChecker: PlatformCapabilityChecker,
    private val triggerSystemStateCache: TriggerSystemStateCache,
    private val logger: Logger
) {
    private val TAG = this::class.java.simpleName

    @Suppress("DEPRECATION", "MissingPermission")
    fun getPairedDevices(): List<BluetoothDeviceInfo> {
        if (!capabilityChecker.canMonitorBluetoothTriggers()) {
            logger.d(TAG, "Bluetooth permissions are not given.")
            return emptyList()
        }

        val connectedAddresses = triggerSystemStateCache.connectedDevices.value
        val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
            ?: return emptyList()

        return adapter.bondedDevices.orEmpty()
            .map {
                BluetoothDeviceInfo(
                    name = it.name,
                    address = it.address,
                    isConnected = it.address in connectedAddresses
                )
            }
            .sortedWith(
                compareByDescending<BluetoothDeviceInfo> { it.isConnected }.thenBy { it.name }
            )
    }
}
