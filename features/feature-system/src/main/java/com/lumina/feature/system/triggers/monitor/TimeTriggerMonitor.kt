package com.lumina.feature.system.triggers.monitor

import com.lumina.domain.profiles.model.TriggerCondition
import com.lumina.feature.system.triggers.TriggerMonitor
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.Calendar

@Singleton
class TimeTriggerMonitor @Inject constructor() : TriggerMonitor {
    internal fun evaluateTimeOfDay(trigger: TriggerCondition, nowMinutes: Int): Boolean {
        val start = trigger.startTimeMinutes ?: return true
        val end = trigger.endTimeMinutes ?: return true

        return if (start <= end) {
            nowMinutes in start..end
        } else {
            nowMinutes !in (end + 1)..<start
        }
    }

    internal fun evaluateDayOfWeek(trigger: TriggerCondition, today: Int): Boolean {
        val days = trigger.daysOfWeek ?: return true
        return today in days
    }

    override fun evaluate(trigger: TriggerCondition): Boolean {
        val calendar = Calendar.getInstance()
        val nowMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        return evaluateTimeOfDay(trigger, nowMinutes) && evaluateDayOfWeek(trigger, today)
    }
}
