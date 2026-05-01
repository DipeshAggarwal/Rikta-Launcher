package com.lumina.core.ui.components.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.theme.primaryContentColor
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration

private const val DATE_PATTERN = "EEE d MMM"

private object DateDefaults {
    val EndPadding = 10.dp
}

@Composable
fun rememberCurrentDate(): String {
    val locale = LocalConfiguration.current.locales[0]

    // Properly update on Locale change.
    val formatter = remember(locale) {
        DateTimeFormatter.ofPattern(DATE_PATTERN, locale)
    }

    var currentDate by remember {
        mutableStateOf(LocalDate.now().format(formatter))
    }

    LaunchedEffect(locale) {
        while (true) {
            currentDate = LocalDate.now().format(formatter)

            val now = LocalDateTime.now()
            val nextMidnight = now.toLocalDate()
                .plusDays(1)
                .atStartOfDay()

            val delayMillis = Duration
                .between(now, nextMidnight)
                .toMillis()
                .coerceAtLeast(0)

            delay(delayMillis)
        }
    }

    return currentDate
}

@Composable
fun Date(
    onDateClick: () -> Unit,
    homeAlignment: Alignment.Horizontal,
    small: Boolean
) {
    val date = rememberCurrentDate()

    Text(
        text = date,
        color = primaryContentColor,
        style = if (small) {
            MaterialTheme.typography.bodyMedium
        } else {
            MaterialTheme.typography.bodyLarge
        },
        fontWeight = FontWeight.W600,
        modifier = Modifier
            .padding(end = DateDefaults.EndPadding)
            .clickable { onDateClick() },
        textAlign = when (homeAlignment) {
            Alignment.Start -> TextAlign.Start
            Alignment.End -> TextAlign.End
            else -> TextAlign.Center
        }
    )
}
