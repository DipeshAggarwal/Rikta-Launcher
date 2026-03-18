package com.lumina.feature.system.triggers

import com.lumina.feature.system.triggers.builder.TriggerConditionBuilder
import com.lumina.feature.system.triggers.monitor.TimeTriggerMonitor
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class TimeTriggerMonitorTest {
    private lateinit var timeTriggerMonitor: TimeTriggerMonitor

    @Before
    fun setup() {
        timeTriggerMonitor = TimeTriggerMonitor()
    }

    @Test
    fun `time is within needed range`() {
        val trigger = TriggerConditionBuilder.time(
            startTimeMinutes = 8 * 60,
            endTimeMinutes = 16 * 60
        )
        assertTrue(timeTriggerMonitor.evaluateTimeOfDay(trigger, 12 * 60))
    }

    @Test
    fun `time is before needed range`() {
        val trigger = TriggerConditionBuilder.time(
            startTimeMinutes = 8 * 60,
            endTimeMinutes = 16 * 60
        )
        assertFalse(timeTriggerMonitor.evaluateTimeOfDay(trigger, 7 * 60))
    }

    @Test
    fun `time is after needed range`() {
        val trigger = TriggerConditionBuilder.time(
            startTimeMinutes = 8 * 60,
            endTimeMinutes = 16 * 60
        )
        assertFalse(timeTriggerMonitor.evaluateTimeOfDay(trigger, 17 * 60))
    }

    @Test
    fun `time is exactly at start of range`() {
        val trigger = TriggerConditionBuilder.time(
            startTimeMinutes = 8 * 60,
            endTimeMinutes = 16 * 60
        )
        assertTrue(timeTriggerMonitor.evaluateTimeOfDay(trigger, 8 * 60))
    }

    @Test
    fun `time is exactly at end of range`() {
        val trigger = TriggerConditionBuilder.time(
            startTimeMinutes = 8 * 60,
            endTimeMinutes = 16 * 60
        )
        assertTrue(timeTriggerMonitor.evaluateTimeOfDay(trigger, 16 * 60))
    }

    @Test
    fun `time is within needed range after midnight`() {
        val trigger = TriggerConditionBuilder.time(
            startTimeMinutes = 22 * 60,
            endTimeMinutes = 6 * 60
        )
        assertTrue(timeTriggerMonitor.evaluateTimeOfDay(trigger, 3 * 60))
    }

    @Test
    fun `time is within needed range before midnight`() {
        val trigger = TriggerConditionBuilder.time(
            startTimeMinutes = 22 * 60,
            endTimeMinutes = 6 * 60
        )
        assertTrue(timeTriggerMonitor.evaluateTimeOfDay(trigger, 23 * 60))
    }

    @Test
    fun `time is not within needed range after midnight`() {
        val trigger = TriggerConditionBuilder.time(
            startTimeMinutes = 22 * 60,
            endTimeMinutes = 6 * 60
        )
        assertFalse(timeTriggerMonitor.evaluateTimeOfDay(trigger, 8 * 60))
    }

    @Test
    fun `time is not within needed range before midnight`() {
        val trigger = TriggerConditionBuilder.time(
            startTimeMinutes = 22 * 60,
            endTimeMinutes = 6 * 60
        )
        assertFalse(timeTriggerMonitor.evaluateTimeOfDay(trigger, 21 * 60))
    }

    @Test
    fun `start and end time matches when null`() {
        val trigger = TriggerConditionBuilder.time(
            startTimeMinutes = null,
            endTimeMinutes = null
        )
        assertTrue(timeTriggerMonitor.evaluateTimeOfDay(trigger, 12 * 60))
    }

    @Test
    fun `today is in allowed days`() {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val trigger = TriggerConditionBuilder.time(daysOfWeek = listOf(today))
        assertTrue(timeTriggerMonitor.evaluateDayOfWeek(trigger, today))
    }

    @Test
    fun `today is not in allowed days`() {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val notToday = if (today == Calendar.SUNDAY) Calendar.MONDAY else Calendar.SUNDAY

        val trigger = TriggerConditionBuilder.time(daysOfWeek = listOf(notToday))
        assertFalse(timeTriggerMonitor.evaluateDayOfWeek(trigger, today))
    }

    @Test
    fun `day matches when null`() {
        val trigger = TriggerConditionBuilder.time(daysOfWeek = null)
        assertTrue(timeTriggerMonitor.evaluateDayOfWeek(trigger, Calendar.SUNDAY))
    }

    @Test
    fun `both time and day natch`() {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val nowMinutes = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60

        val trigger = TriggerConditionBuilder.time(
            startTimeMinutes = nowMinutes - 60,
            endTimeMinutes = nowMinutes + 60,
            daysOfWeek = listOf(today)
        )
        assertTrue(timeTriggerMonitor.evaluate(trigger))
    }

    @Test
    fun `time match but day does not natch`() {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val nowMinutes = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60
        val notToday = if (today == Calendar.SUNDAY) Calendar.MONDAY else Calendar.SUNDAY

        val trigger = TriggerConditionBuilder.time(
            startTimeMinutes = nowMinutes - 60,
            endTimeMinutes = nowMinutes + 60,
            daysOfWeek = listOf(notToday)
        )
        assertFalse(timeTriggerMonitor.evaluate(trigger))
    }
}
