package com.lumina.core.ui.components.settings

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.lumina.core.ui.R
import com.lumina.core.ui.theme.ContentColor
import com.lumina.core.ui.theme.SecondaryContentColor
import com.lumina.core.ui.theme.TrackActiveColor
import com.lumina.core.ui.theme.TrackInactiveColor
import kotlin.math.roundToInt

private object SliderDotTrackDefault {
    val DotRadius = 6.dp
    val TrackActiveStrokeWidth = 6.dp
    val TrackStrokeWidth = 5.dp
    val TrackHeight = 24.dp
}

private object SliderThumbDefault {
    val OuterRadius = 24.dp
    val InnerRadius = 16.dp
    val ShadowElevation = 8.dp
}

private object SettingsSliderRowDefault {
    val MinScaleFactor = 0.7f
    val MaxScaleFactor = 1.0f
    val EndPadding = 8.dp
    val IconAlpha = 0.64f
    val IconSize = 36.dp
    val LabelSpacing = 2.dp
}

@Composable
fun SliderDotTrack(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    modifier: Modifier = Modifier
) {
    val activeColor = TrackActiveColor
    val inactiveColor = TrackInactiveColor

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(SliderDotTrackDefault.TrackHeight)
    ) {
        val trackY = size.height / 2f
        val trackStart = 0f
        val trackEnd = size.width

        val progress = if (steps > 0) {
            (value - valueRange.start) / steps
        } else 0f
        val thumbX = trackStart + (progress * (trackEnd - trackStart))

        // Active Line
        if (thumbX > trackStart) {
            drawLine(
                color = activeColor,
                start = Offset(trackStart, trackY),
                end = Offset(thumbX, trackY),
                strokeWidth = SliderDotTrackDefault.TrackActiveStrokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Inactive Line
        if (thumbX < trackEnd) {
            drawLine(
                color = inactiveColor,
                start = Offset(thumbX, trackY),
                end = Offset(trackEnd, trackY),
                strokeWidth = SliderDotTrackDefault.TrackStrokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        }

        for (i in 0..steps) {
            val stopFraction = i.toFloat() / steps
            val dotX = trackStart + stopFraction * (trackEnd - trackStart)
            val isActive = dotX <= thumbX

            drawCircle(
                color = if (isActive) activeColor else inactiveColor,
                radius = SliderDotTrackDefault.DotRadius.toPx(),
                center = Offset(dotX, trackY)
            )
        }
    }
}

@Composable
fun SliderThumb(modifier: Modifier = Modifier) {
    val innerThumbColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .shadow(
                elevation = SliderThumbDefault.ShadowElevation,
                shape = CircleShape,
                spotColor = innerThumbColor,
                ambientColor = innerThumbColor
            )
            .size(SliderThumbDefault.OuterRadius)
            .clip(CircleShape)
            .background(innerThumbColor),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(SliderThumbDefault.InnerRadius)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimaryContainer)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayName: String,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var resetTrigger by remember { mutableStateOf(false) }
    val resetScale by animateFloatAsState(
        targetValue = if (resetTrigger) SettingsSliderRowDefault.MinScaleFactor else SettingsSliderRowDefault.MaxScaleFactor,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        finishedListener = { resetTrigger = false },
        label = "resetScale"
    )

    val steps = valueRange.endInclusive.roundToInt() - valueRange.start.roundToInt()
    val internalSteps = steps - 1

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SettingsComponentsDefaults.RowVerticalPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$label: $displayName",
                style = MaterialTheme.typography.bodyLarge,
                color = ContentColor,
                maxLines = SettingsComponentsDefaults.MAX_LINES,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(SettingsComponentsDefaults.TEXT_WEIGHT)
                    .padding(end = SettingsSliderRowDefault.EndPadding)
            )
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.reset_to_default),
                tint = SecondaryContentColor.copy(alpha = SettingsSliderRowDefault.IconAlpha),
                modifier = Modifier
                    .size(SettingsSliderRowDefault.IconSize)
                    .scale(resetScale)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        resetTrigger = true
                        onReset()
                    }
            )
        }

        Spacer(modifier = Modifier.height(SettingsSliderRowDefault.LabelSpacing))

        Slider(
            value = value,
            valueRange = valueRange,
            steps = internalSteps,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            modifier = Modifier.fillMaxWidth(),
            thumb = { SliderThumb() },
            track = {
                SliderDotTrack(
                    value,
                    valueRange,
                    steps
                )
            }
        )
    }
}
