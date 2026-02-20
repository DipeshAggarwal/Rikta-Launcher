package com.lumina.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import com.lumina.core.ui.R

enum class TimeFormat {
    STANDARD, // 1h 1m
    CLOCK, // 01:01:00
    HOURS, // 1h
    MINUTES, // 61m
}

@Composable
fun formatDuration(
    milliseconds: Long,
    format: TimeFormat = TimeFormat.STANDARD
): String {
    if (milliseconds <= 0) {
        return stringResource(R.string.duration_zero)
    }

    val totalSeconds = milliseconds / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when(format) {
        TimeFormat.STANDARD -> {
            when {
                hours > 0 -> {
                    val h = pluralStringResource(
                        R.plurals.duration_hours_short,
                        hours.toInt(),
                        hours
                    )
                    val m = if (minutes > 0) {
                        pluralStringResource(
                            R.plurals.duration_minutes_short,
                            minutes.toInt(),
                            minutes
                        )
                    } else {
                        ""
                    }
                    "$h $m"
                }

                minutes > 0 -> {
                    pluralStringResource(
                        R.plurals.duration_minutes_short,
                        minutes.toInt(),
                        minutes
                    )
                }

                else -> {
                    pluralStringResource(
                        R.plurals.duration_seconds_short,
                        seconds.toInt(),
                        seconds
                    )
                }
            }
        }

        TimeFormat.CLOCK -> {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        }

        TimeFormat.HOURS -> {
            pluralStringResource(
                R.plurals.duration_hours_short,
                hours.toInt(),
                hours
            )
        }

        TimeFormat.MINUTES -> {
            val totalMinutes = totalSeconds / 60
            pluralStringResource(
                R.plurals.duration_minutes_short,
                totalMinutes.toInt(),
                totalMinutes
            )
        }
    }
}

@Composable
fun DurationText(
    milliseconds: Long,
    color: Color,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    format: TimeFormat = TimeFormat.STANDARD
) {
    Text(
        text = formatDuration(milliseconds, format),
        color = color,
        modifier = modifier,
        style = style
    )
}
