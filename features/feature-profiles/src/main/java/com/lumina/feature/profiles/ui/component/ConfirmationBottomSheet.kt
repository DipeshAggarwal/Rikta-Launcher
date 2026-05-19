package com.lumina.feature.profiles.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.lumina.core.ui.ThemeTokens
import com.lumina.core.ui.components.StandardBottomSheet

@Composable
fun ConfirmationBottomSheet(
    icon: ImageVector,
    title: String,
    subtitle: String,
    confirmText: String,
    confirmColor: Color,
    cancelText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    StandardBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) { dismissAction ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ThemeTokens.Spacing.ExtraLarge,
                    vertical = ThemeTokens.Spacing.Large
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(ThemeTokens.Icon.ContainerSize)
                    .background(
                        color = confirmColor.copy(alpha = ThemeTokens.Alpha.Light),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = confirmColor,
                    modifier = Modifier.size(ThemeTokens.Icon.TertiaryIconSize)
                )
            }

            Spacer(modifier = Modifier.height(ThemeTokens.Spacing.ExtraLarge))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(ThemeTokens.Spacing.Medium))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(ThemeTokens.Spacing.ExtraLarge))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ThemeTokens.Spacing.Large),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { dismissAction(onDismiss) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = cancelText,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(
                    onClick = { dismissAction(onConfirm) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(contentColor = confirmColor)
                ) {
                    Text(
                        text = confirmText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(ThemeTokens.Spacing.Large))
        }
    }
}
