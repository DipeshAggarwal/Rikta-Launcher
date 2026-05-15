package com.lumina.core.common.time

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeUtils {
    fun getCurrentTimeParts(twelveHourDisplay: Boolean): Triple<Int, Int, Boolean> {
        val now = LocalTime.now()

        val isPm = now.hour >= 12
        val hour = if (twelveHourDisplay) {
            val h = now.hour % 12
            if (h == 0) 12 else h
        } else {
            now.hour
        }

        return Triple(hour, now.minute, isPm)
    }

    fun formatTimestamp(epochMillis: Long): String {
        return DateTimeFormatter
            .ofPattern("dd MMM, yyyy, hh:mm a", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(epochMillis))
    }
}
