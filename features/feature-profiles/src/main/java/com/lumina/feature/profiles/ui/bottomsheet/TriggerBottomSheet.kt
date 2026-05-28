package com.lumina.feature.profiles.ui.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.lumina.core.model.ProfileTriggerType
import com.lumina.core.ui.ThemeTokens
import com.lumina.core.ui.components.StandardBottomSheet
import com.lumina.feature.profiles.R
import com.lumina.feature.profiles.extensions.displayName
import com.lumina.feature.profiles.extensions.subtitle

@Composable
fun TriggerTypePickerBottomSheet(
    onSelect: (ProfileTriggerType) -> Unit,
    onDismiss: () -> Unit
) {
    StandardBottomSheet(onDismissRequest = onDismiss) { dismissAction ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ThemeTokens.Spacing.ExtraLarge,
                    vertical = ThemeTokens.Spacing.Large
                ),
            verticalArrangement = Arrangement.spacedBy(ThemeTokens.Spacing.Small)
        ) {
            Text(
                text = stringResource(R.string.profile_trigger_type_picker_heading),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(
                        horizontal = ThemeTokens.Spacing.ExtraLarge,
                        vertical = ThemeTokens.Spacing.Large
                    )
            )
            Spacer(modifier = Modifier.height(ThemeTokens.Spacing.Small))

            ProfileTriggerType.entries.forEachIndexed { index, type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { dismissAction { onSelect(type) } } )
                        .padding(
                            horizontal = ThemeTokens.Spacing.ExtraLarge,
                            vertical = ThemeTokens.Spacing.Medium
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = type.displayName(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = type.subtitle(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(ThemeTokens.Spacing.Small))

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(ThemeTokens.Icon.TertiaryIconSize)
                    )
                }

                if (index < ProfileTriggerType.entries.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = ThemeTokens.Spacing.Small),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(ThemeTokens.Spacing.ExtraLarge))
        }
    }
}
