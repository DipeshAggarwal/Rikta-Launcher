package com.lumina.feature.system.triggers.monitor

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
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

@Singleton
class BluetoothTriggerMonitor @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:ApplicationScope private val scope: CoroutineScope,
    private val triggerSystemStateCache: TriggerSystemStateCache,
    private val capabilityChecker: PlatformCapabilityChecker,
    private val logger: Logger
) : TriggerMonitor {
    private val TAG = this::class.java.simpleName

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val device = IntentCompat.getParcelableExtra(
                intent ?: return,
                BluetoothDevice.EXTRA_DEVICE,
                BluetoothDevice::class.java
            ) ?: return

            val address = device.address
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    logger.d(TAG, "Bluetooth connected: ${address}. Evaluating Triggers.")
                    scope.launch { triggerSystemStateCache.addConnectedDevice(address) }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    logger.d(TAG, "Bluetooth disconnected: ${address}. Evaluating Triggers.")
                    scope.launch { triggerSystemStateCache.removeConnectedDevice(address) }
                }
            }
        }
    }

    // This Suppress is very intentional here as PlatformCapabilityChecker should handle all permission.
    @Suppress("MissingPermission")
    fun register() {
        if (!capabilityChecker.canMonitorBluetoothTriggers()) {
            logger.d(TAG, "Skipping Bluetooth registration because of missing permission.")
            return
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }

        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        logger.d(TAG, "Bluetooth Receiver registered.")
    }

    fun unregister() {
        try {
            context.unregisterReceiver(receiver)
            triggerSystemStateCache.clearConnectedDevices()

            logger.d(TAG, "Bluetooth Receiver unregistered.")
        } catch (e: Exception) {
            logger.w(TAG, "Bluetooth Receiver couldn't unregister.", e)
        }
    }

    override fun evaluate(trigger: TriggerCondition): Boolean {
        val targetAddress = trigger.bluetoothAddress ?: return false
        return triggerSystemStateCache.connectedDevices.value.contains(targetAddress)
    }
}