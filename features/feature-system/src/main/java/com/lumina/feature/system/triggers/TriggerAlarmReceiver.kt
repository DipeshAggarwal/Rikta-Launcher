package com.lumina.feature.system.triggers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lumina.core.android.di.ApplicationScope
import com.lumina.core.logging.Logger
import com.lumina.feature.system.triggers.monitor.TimeTriggerScheduler
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TriggerAlarmReceiver : BroadcastReceiver() {
    @Inject @ApplicationScope lateinit var scope: CoroutineScope

    @Inject lateinit var triggerEvaluationEngine: TriggerEvaluationEngine
    @Inject lateinit var timeTriggerScheduler: TimeTriggerScheduler
    @Inject lateinit var logger: Logger

    private val TAG = this::class.java.simpleName

    override fun onReceive(p0: Context?, p1: Intent?) {
        logger.d(TAG, "Alarm triggered. Evaluating Trigger.")
        scope.launch {
            triggerEvaluationEngine.evaluateTriggers()
            timeTriggerScheduler.scheduleNextAlarm()
        }
    }
}
