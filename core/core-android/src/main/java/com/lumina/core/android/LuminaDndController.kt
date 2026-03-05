package com.lumina.core.android

import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class LuminaDndController @Inject constructor(
    @param:ApplicationContext private val context: Context
){
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    fun hasDndAccess(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

    fun isDndEnabled(): Boolean {
        return notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
    }

    fun setDndEnabled(enabled: Boolean, filter: Int = NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
        if (!hasDndAccess()) return
        notificationManager.setInterruptionFilter(
            if (enabled) filter else NotificationManager.INTERRUPTION_FILTER_ALL
        )
    }
}
