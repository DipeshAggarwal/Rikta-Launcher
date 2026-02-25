package com.lumina.core.android

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import com.lumina.core.common.SystemActions
import com.lumina.core.logging.Logger
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

// Knowingly suppressed.
// Planned: Double tap to lock and other accessibility actions.
@SuppressLint("AccessibilityPolicy")
@AndroidEntryPoint
class LuminaAccessibilityService: AccessibilityService() {
    @Inject
    lateinit var logger: Logger
    private val TAG = this::class.java.simpleName

    private val expandReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            println("DEBUG_DEBUG, $intent")
            when (intent?.action) {
                SystemActions.EXPAND_NOTIFICATION_SHADE -> {
                    performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        println("DEBUG_DEBUG: Service connected.")

        val filter = IntentFilter(SystemActions.EXPAND_NOTIFICATION_SHADE)
        ContextCompat.registerReceiver(
            this,
            expandReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(expandReceiver)
            logger.d("$TAG:Destroy", "Receiver cleanly unregistered")
        } catch (e: Exception) {
            logger.w("$TAG:Destroy", "Receiver not registered when destroying service.", e)
        }
    }
}