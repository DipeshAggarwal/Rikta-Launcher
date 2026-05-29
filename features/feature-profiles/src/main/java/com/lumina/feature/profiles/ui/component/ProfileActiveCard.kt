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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumina.core.model.ProfileClassification
import com.lumina.core.ui.ThemeTokens

@Composable
fun ProfileActiveCard(
    title: String,
    profileClassification: ProfileClassification,
    appCount: Int,
    triggerCount: Int,
    profileId: String,
    iconName: String?,
    activeText: String,
    optionsDescription: String,
    onClick: () -> Unit,
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
                    horizontal = ThemeTokens.Spacing.ExtraLarge,
                    vertical = ThemeTokens.Spacing.Large
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileIcon(
                profileName = title,
                profileId = profileId,
                iconName = iconName,
                size = ThemeTokens.Icon.PrimarySize
            )
            Spacer(modifier = Modifier.width(ThemeTokens.Spacing.Large))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.width(ThemeTokens.Spacing.Medium))
                    Box(
                        modifier = Modifier
                            .size(ThemeTokens.Spacing.Medium)
                            .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(ThemeTokens.Spacing.Small))
                    Text(
                        text = activeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Spacer(modifier = Modifier.height(ThemeTokens.Spacing.Tiny))

                ProfileSubtitle(
                    classification = profileClassification,
                    appCount = appCount,
                    triggerCount = triggerCount,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = ThemeTokens.Alpha.Heavy)
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = optionsDescription,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
