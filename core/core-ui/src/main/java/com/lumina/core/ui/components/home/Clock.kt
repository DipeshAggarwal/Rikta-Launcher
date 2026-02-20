package com.lumina.core.ui.components.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lumina.core.common.time.TimeUtils.getCurrentTimeParts
import com.lumina.core.ui.theme.primaryContentColor
import kotlinx.coroutines.delay
import java.time.LocalTime

private const val BIG_TIME_FORMAT = "%02d\n%02d"
private const val SMALL_TIME_FORMAT = "%02d:%02d"

@Composable
fun rememberCurrentTimeParts(twelveHourDisplay: Boolean): Triple<Int, Int, Boolean> {
    var timeParts by remember { mutableStateOf(getCurrentTimeParts(twelveHourDisplay)) }

    LaunchedEffect(twelveHourDisplay) {
        while (true) {
            timeParts = getCurrentTimeParts(twelveHourDisplay)

            val now = LocalTime.now()
            // The nano second subtraction is to avoid drifting in extreme cases.
            val millisUntilNextMinute = (60 - now.second) * 1000L - (now.nano / 1_000_000L)

            // Ensure it is never in negative.
            val delayMillis = millisUntilNextMinute.coerceAtLeast(0L)

            delay(delayMillis)
        }
    }

    return timeParts
}

@Composable
fun Clock(
    bigClock: Boolean,
    twelveHourDisplay: Boolean,
    onClockClick: () -> Unit,
    homeAlignment: Alignment.Horizontal
) {
    val (hour, minute, _) = rememberCurrentTimeParts(twelveHourDisplay)

    if (bigClock) {
        Text(
            text = BIG_TIME_FORMAT.format(hour, minute),
            modifier = Modifier.clickable { onClockClick() },
            color = primaryContentColor,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFeatureSettings = "tnum"
            ),
            textAlign = TextAlign.Center
        )
    } else {
        Text(
            text = SMALL_TIME_FORMAT.format(hour, minute),
            color = primaryContentColor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .offset((-2).dp, 5.dp)
                .clickable { onClockClick() },
            textAlign = when (homeAlignment) {
                Alignment.Start -> TextAlign.Start
                Alignment.End -> TextAlign.End
                else -> TextAlign.Center
            }
        )
    }
}
