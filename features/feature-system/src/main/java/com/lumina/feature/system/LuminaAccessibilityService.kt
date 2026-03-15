package com.lumina.feature.system

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Process.myUserHandle
import android.os.UserManager
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import com.lumina.core.android.di.ApplicationScope
import com.lumina.core.common.SystemActions
import com.lumina.core.logging.Logger
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.core.model.SystemProfileIds
import com.lumina.domain.usage.AppUsageTracker
import com.lumina.domain.usage.UsageRepository
import com.lumina.domain.usage.model.UsageTimeRange
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

// Knowingly suppressed.
// Planned: Double tap to lock and other accessibility actions.
@SuppressLint("AccessibilityPolicy")
@AndroidEntryPoint
class LuminaAccessibilityService : AccessibilityService() {
    @Inject @ApplicationScope lateinit var scope: CoroutineScope
    @Inject lateinit var profileRepository: ProfileRepository
    @Inject lateinit var usageRepository: UsageRepository
    @Inject lateinit var appUsageTracker: AppUsageTracker
    @Inject lateinit var userManager: UserManager
    @Inject lateinit var usageEnforcementEngine: UsageEnforcementEngine
    @Inject lateinit var logger: Logger

    private var currentProfileId: String = SystemProfileIds.DEFAULT
    private var lastForegroundPackage: String? = null

    private val TAG = this::class.java.simpleName

    private val expandReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                SystemActions.EXPAND_NOTIFICATION_SHADE -> {
                    performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
        observeActiveProfile()

        val filter = IntentFilter(SystemActions.EXPAND_NOTIFICATION_SHADE)
        ContextCompat.registerReceiver(
            this,
            expandReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == lastForegroundPackage) return

        val timestamp = System.currentTimeMillis()
        val userHandleNumber = getUserHandleNumber()

        lastForegroundPackage?.let {
            appUsageTracker.onAppBackgrounded(it, userHandleNumber, timestamp)
        }

        appUsageTracker.onAppForegrounded(packageName, userHandleNumber, currentProfileId, timestamp)
        lastForegroundPackage = packageName

        usageEnforcementEngine.startMonitoring(
            scope, packageName, getUserHandleNumber(), currentProfileId
        ) {
            // stuff
        }
    }

    override fun onInterrupt() {
        logger.w(TAG, "Accessibility service interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(expandReceiver)
            logger.d("$TAG:Destroy", "Receiver cleanly unregistered")
        } catch (e: Exception) {
            logger.w("$TAG:Destroy", "Receiver not registered when destroying service.", e)
        }
    }

    private fun observeActiveProfile() {
        profileRepository.activeProfile
            .onEach { profile ->
                val newProfileId = profile?.id ?: SystemProfileIds.DEFAULT
                if (newProfileId != currentProfileId) {
                    val timestamp = System.currentTimeMillis()
                    val userHandleNumber = getUserHandleNumber()

                    lastForegroundPackage?.let {
                        appUsageTracker.onAppBackgrounded(
                            it, userHandleNumber, timestamp
                        )
                    }

                    appUsageTracker.onProfileSwitched(newProfileId, timestamp)
                    currentProfileId = newProfileId

                    lastForegroundPackage?.let {
                        appUsageTracker.onAppForegrounded(
                            it,
                            userHandleNumber,
                            newProfileId,
                            timestamp
                        )

                        usageEnforcementEngine.startMonitoring(
                            scope, packageName, getUserHandleNumber(), currentProfileId
                        ) {
                            // stuff
                        }
                    }
                }
            }
            .launchIn(scope)
    }

    private fun getUserHandleNumber(): Long = userManager.getSerialNumberForUser(myUserHandle())
}