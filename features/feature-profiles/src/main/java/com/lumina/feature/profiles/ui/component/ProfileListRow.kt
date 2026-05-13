package com.lumina.feature.profiles.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lumina.core.model.ProfileClassification
import com.lumina.core.ui.ThemeTokens

private object ProfileListRowDefaults {
    val SubtitleTextColorAlpha = 0.8f
}

@Composable
fun ProfileListRow(
    title: String,
    profileClassification: ProfileClassification,
    appCount: Int,
    triggerCount: Int,
    profileId: String,
    iconName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
            size = ThemeTokens.Icon.SecondaryIconSize
        )
        Spacer(modifier = Modifier.width(ThemeTokens.Spacing.Large))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            ProfileSubtitle(
                classification = profileClassification,
                appCount = appCount,
                triggerCount = triggerCount,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ProfileListRowDefaults.SubtitleTextColorAlpha)
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
