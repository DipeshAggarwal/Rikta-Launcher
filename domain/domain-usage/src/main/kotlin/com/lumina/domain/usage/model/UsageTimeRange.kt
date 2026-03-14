package com.lumina.domain.usage.model

import java.util.Calendar

enum class UsageTimeRange {
    TODAY,
    YESTERDAY,
    TWO_DAYS_AGO,
    CURRENT_WEEK,
    LAST_WEEK,
    ALL_TIME;

    fun toEpochBounds(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return when (this) {
            UsageTimeRange.TODAY -> {
                Pair(calendar.timeInMillis, now)
            }

            UsageTimeRange.YESTERDAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val start = calendar.timeInMillis

                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val end = calendar.timeInMillis - 1
                Pair(start, end)
            }

            UsageTimeRange.TWO_DAYS_AGO -> {
                calendar.add(Calendar.DAY_OF_YEAR, -2)
                val start = calendar.timeInMillis

                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val end = calendar.timeInMillis - 1
                Pair(start, end)
            }

            UsageTimeRange.CURRENT_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                Pair(calendar.timeInMillis, now)
            }

            UsageTimeRange.LAST_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                val start = calendar.timeInMillis

                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                val end = calendar.timeInMillis - 1
                Pair(start, end)
            }

            UsageTimeRange.ALL_TIME -> {
                Pair(0L, now)
            }
        }
    }
}
