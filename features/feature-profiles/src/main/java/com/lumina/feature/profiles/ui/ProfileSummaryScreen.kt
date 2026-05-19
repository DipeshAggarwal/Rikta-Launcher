package com.lumina.feature.profiles.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lumina.core.common.time.TimeUtils
import com.lumina.core.model.ProfileClassification
import com.lumina.core.ui.Motion
import com.lumina.core.ui.ThemeTokens
import com.lumina.core.ui.components.StandardListScaffold
import com.lumina.core.ui.extensions.accentColourRes
import com.lumina.core.ui.extensions.displayName
import com.lumina.core.ui.extensions.icon
import com.lumina.core.ui.extensions.systemProfileDisplayName
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.feature.profiles.ProfilesDefaults
import com.lumina.feature.profiles.R
import com.lumina.feature.profiles.ui.component.InLineTextField
import com.lumina.feature.profiles.ui.component.ProfileIcon

@Composable
fun ProfileSummaryScreen(
    profile: LauncherProfile,
    classification: ProfileClassification,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val resolvedName = systemProfileDisplayName(profile.name)

    StandardListScaffold(
        title = stringResource(R.string.profile_summary_header),
        onBack = onBack
    ) {
        item(key = "profile_summary_subtitle") {
            Text(
                text = stringResource(R.string.profile_summary_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item(key = "profile_summary_text_fields") {
            NameDescriptionCard(
                name = resolvedName,
                description = profile.description ?: "",
                onNameChange = onNameChange,
                onDescriptionChange = onDescriptionChange
            )
        }

        item(key = "profile_summary_info_details") {
            SummaryInfoCard(
                profile = profile,
                resolvedName = resolvedName,
                classification = classification
            )
        }

        item(key = "profile_summary_time_details") {
            SummaryTimeCard(profile = profile)
        }
    }
}

@Composable
private fun NameDescriptionCard(
    name: String,
    description: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var containerFocused by rememberSaveable { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (containerFocused) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(Motion.SINGlE_ELEMENT_TRANSITION_MS),
        label = "profile_summary_border"
    )

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        InLineTextField(
            label = stringResource(R.string.create_profile_add_name_label),
            value = name,
            maxLength = ProfilesDefaults.NAME_MAX_CHARACTERS,
            maxLines = 1,
            onValueChange = {
                if (it.length <= ProfilesDefaults.NAME_MAX_CHARACTERS) onNameChange(it)
            },
            onFocusChange = { containerFocused = it },
            imeAction = ImeAction.Next,
            labelColor = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = ThemeTokens.Spacing.ExtraLarge),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        InLineTextField(
            label = stringResource(R.string.create_profile_add_description_label),
            value = description,
            maxLength = ProfilesDefaults.DESCRIPTION_MAX_CHARACTERS,
            maxLines = 3,
            onValueChange = {
                if (it.length <= ProfilesDefaults.DESCRIPTION_MAX_CHARACTERS) onDescriptionChange(it)
            },
            onFocusChange = { containerFocused = it },
            imeAction = ImeAction.Done,
            labelColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SummaryInfoCard(
    profile: LauncherProfile,
    resolvedName: String,
    classification: ProfileClassification,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        SummaryInfoLabelRow(label = stringResource(R.string.profile_summary_icon_label)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileIcon(
                    profileName = resolvedName,
                    profileId = profile.id,
                    iconName = profile.overrides.iconName,
                    size = ThemeTokens.Icon.PrimaryIconSize
                )
                Spacer(modifier = Modifier.width(ThemeTokens.Spacing.Large))
                Text(
                    text = profile.overrides.iconName
                        ?: stringResource(R.string.profile_summary_icon_default),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = ThemeTokens.Spacing.ExtraLarge),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        SummaryInfoLabelRow(label = stringResource(R.string.profile_summary_preset_label)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = classification.preset.icon(),
                    contentDescription = null,
                    tint = classification.preset.accentColourRes(),
                    modifier = Modifier.size(ThemeTokens.Icon.PrimaryIconSize)
                )
                Spacer(modifier = Modifier.width(ThemeTokens.Spacing.Large))

                Text(
                    text = classification.preset.displayName(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun SummaryTimeCard(
    profile: LauncherProfile,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        SummaryInfoTimestampRow(
            label = stringResource(R.string.profile_summary_created_label),
            epochMillis = profile.createdAt,
            icon = Icons.Outlined.AccessTime
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = ThemeTokens.Spacing.ExtraLarge),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        SummaryInfoTimestampRow(
            label = stringResource(R.string.profile_summary_updated_label),
            epochMillis = profile.updatedAt,
            icon = Icons.Outlined.Autorenew
        )
    }
}

@Composable
private fun SummaryInfoLabelRow(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = ThemeTokens.Spacing.ExtraLarge,
                vertical = ThemeTokens.Spacing.Large
            ),
        verticalArrangement = Arrangement.spacedBy(ThemeTokens.Spacing.Small)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
private fun SummaryInfoTimestampRow(
    label: String,
    epochMillis: Long,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val formatedTime = TimeUtils.formatTimestamp(epochMillis)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = ThemeTokens.Spacing.ExtraLarge,
                vertical = ThemeTokens.Spacing.Large
            ),
        verticalArrangement = Arrangement.spacedBy(ThemeTokens.Spacing.Small)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(ThemeTokens.Icon.BannerCloseIconSize)
            )
            Spacer(modifier = Modifier.width(ThemeTokens.Spacing.Medium))

            Text(
                text = formatedTime,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
