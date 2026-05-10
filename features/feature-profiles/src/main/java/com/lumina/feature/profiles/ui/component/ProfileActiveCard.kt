package com.lumina.feature.profiles.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.ThemeDimensions

private object ProfileActiveCardDefaults {
    val SubtitleTextColorAlpha = 0.8f
}

@Composable
fun ProfileActiveCard(
    title: String,
    subtitle: String,
    profileId: String,
    iconName: String?,
    activeText: String,
    optionsDescription: String,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ThemeDimensions.Spacing.ExtraLarge,
                    vertical = ThemeDimensions.Spacing.Large
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileIcon(
                profileName = title,
                profileId = profileId,
                iconName = iconName,
                size = ThemeDimensions.Icon.PrimaryIconSize
            )
            Spacer(modifier = Modifier.width(ThemeDimensions.Spacing.Large))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.width(ThemeDimensions.Spacing.Medium))
                    Box(
                        modifier = Modifier
                            .size(ThemeDimensions.Spacing.Medium)
                            .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(ThemeDimensions.Spacing.Small))
                    Text(
                        text = activeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Spacer(modifier = Modifier.height(ThemeDimensions.Spacing.Tiny))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = ProfileActiveCardDefaults.SubtitleTextColorAlpha)
                )
            }

            IconButton(onClick = onOptionsClick) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = optionsDescription,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
