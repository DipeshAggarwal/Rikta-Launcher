package com.lumina.feature.profiles.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.lumina.core.ui.ThemeTokens
import kotlin.math.absoluteValue

private object ProfileIconDefaults {
    val BackgroundAlpha = 0.2f
}

@Composable
fun ProfileIcon(
    profileName: String,
    profileId: String,
    iconName: String?,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val iconColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary
    )
    val colorIndex = profileId.hashCode().absoluteValue % iconColors.size
    val backgroundColor = iconColors[colorIndex]

    Box(
        modifier = modifier
            .size(size)
            .background(
                backgroundColor.copy(alpha = ProfileIconDefaults.BackgroundAlpha),
                RoundedCornerShape(ThemeTokens.DefaultCornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = profileName.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = backgroundColor
        )
    }
}
