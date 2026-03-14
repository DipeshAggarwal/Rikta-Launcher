package com.lumina.feature.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.lumina.core.common.BootHandler
import com.lumina.core.logging.Logger
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class LuminaBootReceiver : BroadcastReceiver() {
    @Inject lateinit var bootHandlers: Set<@JvmSuppressWildcards BootHandler>
    @Inject lateinit var logger: Logger

    private val TAG = this::class.java.simpleName

    override fun onReceive(p0: Context?, p1: Intent?) {
        if (p1?.action != Intent.ACTION_LOCKED_BOOT_COMPLETED) return

        val rebootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime()
        bootHandlers.forEach { it.onBoot(rebootTime) }
        logger.d(TAG, "Boot completed.")
    }
}
