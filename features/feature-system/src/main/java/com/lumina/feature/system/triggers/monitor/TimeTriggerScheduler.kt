package com.lumina.feature.system.triggers.monitor

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.lumina.core.logging.Logger
import com.lumina.core.model.ProfileTriggerType
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.TriggerCondition
import com.lumina.feature.system.triggers.TriggerAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.first
import java.util.Calendar

private const val ALARM_REQUEST_CODE = 1024

@Singleton
class TimeTriggerScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val profileRepository: ProfileRepository,
    private val logger: Logger
) {
    private val TAG = this::class.java.simpleName
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun scheduleNextAlarm() {
        val profiles = profileRepository.getAllProfiles().first()
        val allTimeTriggers = mutableListOf<TriggerCondition>()

        profiles.forEach { profile ->
            profileRepository.getProfileTriggers(profile.id).first()
                .filter {
                    it.triggerType == ProfileTriggerType.TIME ||
                    it.triggerType == ProfileTriggerType.DAY
                }
                .forEach { allTimeTriggers.add(it) }
        }

        if (allTimeTriggers.isEmpty()) {
            logger.d(TAG, "No profile has time triggers.")
            return
        }

        val nextTriggerTime = computeNextTriggerTime(allTimeTriggers) ?: return
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            nextTriggerTime,
            getPendingIntent()
        )
        logger.d(TAG, "Next trigger alarm scheduled at $nextTriggerTime.")
    }

    private fun computeNextTriggerTime(triggers: List<TriggerCondition>): Long? {
        val calendar = Calendar.getInstance()
        val nowMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

        val boundaries = mutableListOf<Int>()
        triggers.forEach { trigger ->
            trigger.startTimeMinutes?.let { boundaries.add(it) }
            trigger.endTimeMinutes?.let { boundaries.add(it) }
        }

        if (boundaries.isEmpty()) return null
        val nextMinutes = boundaries.filter { it > nowMinutes }.minOrNull() ?: boundaries.min()

        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, nextMinutes / 60)
            set(Calendar.MINUTE, nextMinutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (nextMinutes <= nowMinutes) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, TriggerAlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun cancel() {
        alarmManager.cancel(getPendingIntent())
        logger.d(TAG, "Trigger alarm cancelled.")
    }
}
