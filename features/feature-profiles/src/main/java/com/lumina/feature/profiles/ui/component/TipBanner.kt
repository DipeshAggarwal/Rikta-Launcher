package com.lumina.feature.profiles.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.ThemeDimensions
import com.lumina.feature.profiles.R

private object TipBannerDefaults {
    val InfoContainerAlpha = 0.4f
    val InfoContainerContentAlpha = 0.7f
}

@Composable
fun TipBanner(
    tipText: String,
    modifier: Modifier = Modifier,
    iconVector: ImageVector = Icons.Outlined.TouchApp
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = TipBannerDefaults.InfoContainerAlpha
            )
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ThemeDimensions.Spacing.ExtraLarge,
                    vertical = ThemeDimensions.Spacing.Large
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = TipBannerDefaults.InfoContainerContentAlpha
                ),
                modifier = Modifier.size(ThemeDimensions.Icon.BannerIconSize)
            )
            Spacer(modifier = Modifier.width(ThemeDimensions.Spacing.Medium))

            Text(
                text = tipText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = TipBannerDefaults.InfoContainerContentAlpha
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = ThemeDimensions.Spacing.ExtraLarge,
                        vertical = ThemeDimensions.Spacing.Large
                    )
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }
    }
}
