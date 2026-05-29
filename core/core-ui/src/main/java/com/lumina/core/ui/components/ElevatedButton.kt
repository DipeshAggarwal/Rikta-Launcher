package com.lumina.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.ThemeTokens

@Composable
fun ElevatedButton(
    title: String,
    subtitle: String?,
    enabled: Boolean,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentAlpha = if (enabled) 1f else ThemeTokens.Alpha.Medium
    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
    val iconColor = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha)

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(
            alpha = contentAlpha)
        ),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(
                    horizontal = ThemeTokens.Spacing.ExtraLarge,
                    vertical = ThemeTokens.Spacing.Medium
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(
                        if (subtitle != null) ThemeTokens.Icon.LeadingSize
                        else ThemeTokens.Icon.BannerIconSize
                    )
                )
                Spacer(modifier = Modifier.size(ThemeTokens.Spacing.Large))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = subtitleColor
                    )
                }
            }

            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(ThemeTokens.Icon.BannerIconSize)
                )
            }
        }
    }
}
