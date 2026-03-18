package com.lumina.feature.system.triggers.monitor

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresPermission
import com.lumina.core.android.di.ApplicationScope
import com.lumina.core.logging.Logger
import com.lumina.domain.profiles.model.TriggerCondition
import com.lumina.feature.system.triggers.TriggerMonitor
import com.lumina.feature.system.triggers.TriggerSystemStateCache
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
    private val logger: Logger
) : TriggerMonitor {
    private val TAG = this::class.java.simpleName

    @Suppress("DEPRECATION")
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                p1?.getParcelableExtra(
                    BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
                )
            } else {
                p1?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            }

            val address = device?.address ?: return
            when (p1?.action) {
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun register() {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }

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