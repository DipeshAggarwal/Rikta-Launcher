package com.lumina.data.coordination

import android.content.Context
import android.content.Intent
import com.lumina.domain.coordination.StatusBarController
import com.lumina.core.common.SystemActions.EXPAND_NOTIFICATION_SHADE
import com.lumina.core.logging.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class PlatformStatusBarController @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val logger: Logger
) : StatusBarController {
    private val TAG = this::class.java.simpleName

    override fun expandNotificationShade() {
        try {
            val intent = Intent(EXPAND_NOTIFICATION_SHADE).apply {
                setPackage(context.packageName)
            }
            println("DEBUG_DEBUG: Context")
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to open notification shade.", e)
        }
    }
}
