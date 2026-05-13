package com.lumina.feature.profiles.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import com.lumina.core.model.ProfileClassification
import com.lumina.core.ui.ThemeTokens
import com.lumina.core.ui.extensions.displayLabel
import com.lumina.feature.profiles.R
import androidx.compose.ui.platform.LocalResources

private object SeparatorDotDefaults {
    const val DOT_SIZE_FRACTION = 0.5f
}

@Composable
fun ProfileSubtitle(
    classification: ProfileClassification,
    appCount: Int,
    triggerCount: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val resources = LocalResources.current

    val modeLabel = classification.displayLabel()
    val appLabel = remember(appCount) {
        resources.getQuantityString(R.plurals.profile_app_count, appCount, appCount)
    }
    val triggerLabel = remember(triggerCount) {
        resources.getQuantityString(R.plurals.profile_trigger_count, triggerCount, triggerCount)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ThemeTokens.Spacing.Small)
    ) {
        Text(text = modeLabel, style = style, color = color)
        SeparatorDot(color = color, textStyle = style)
        Text(text = appLabel, style = style, color = color)
        SeparatorDot(color = color, textStyle = style)
        Text(text = triggerLabel, style = style, color = color)
    }
}

@Composable
private fun SeparatorDot(
    color: Color,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val dotSizeDp = with(density) {
        (textStyle.fontSize.toPx() * SeparatorDotDefaults.DOT_SIZE_FRACTION).toDp()
    }

    Canvas(
        modifier = modifier.size(dotSizeDp)
    ) {
        drawCircle(color = color, radius = size.minDimension / 2f)
    }
}
