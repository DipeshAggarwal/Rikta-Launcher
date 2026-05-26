package com.lumina.feature.profiles.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lumina.core.model.ProfileClassification
import com.lumina.core.ui.ThemeTokens
import com.lumina.core.ui.components.StandardListScaffold
import com.lumina.core.ui.extensions.systemProfileDisplayName
import com.lumina.domain.profiles.model.ProfileSummary
import com.lumina.feature.profiles.R
import com.lumina.core.ui.components.InfoBanner
import com.lumina.feature.profiles.ui.component.ProfileActiveCard
import com.lumina.feature.profiles.ui.component.ProfileListRow
import com.lumina.feature.profiles.ui.component.SectionLabel

private object EmptyProfilesCardDefaults {
    val TextTopPadding = 32.dp
    val TextLeftPadding = 24.dp
    val InactiveElementAlpha = 0.5f
}

@Composable
fun ProfileListScreen(
    activeProfileSummary: ProfileSummary?,
    inactiveProfileSummaries: List<ProfileSummary>,
    profileClassificationMap: Map<String, ProfileClassification>,
    showBanner: Boolean,
    onDismissBanner: () -> Unit,
    onViewProfileDetails: (String) -> Unit,
    onCreateNewProfile: () -> Unit,
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val profileAlreadyActive = stringResource(R.string.profile_already_active)

    StandardListScaffold(
        title = stringResource(R.string.profiles_header),
        onBack = onBack,
        snackbarHostState = snackbarHostState
    ) {
        activeProfileSummary?.let { summary ->
            item(key = "active_profile_section") {
                Column {
                    SectionLabel(text = stringResource(R.string.active_profile))
                }
                ProfileActiveCard(
                    title = systemProfileDisplayName(summary.profile.name),
                    profileClassification = profileClassificationMap.getValue(summary.profile.id),
                    appCount = summary.appCount,
                    triggerCount = summary.triggerCount,
                    profileId = summary.profile.id,
                    iconName = summary.profile.overrides.iconName,
                    activeText = stringResource(R.string.active_now),
                    optionsDescription = stringResource(R.string.profile_options_description),
                    onClick = { onViewProfileDetails(summary.profile.id) }
                )
            }
        }

        if (showBanner) {
            item(key = "info_banner") {
                InfoBanner(
                    bannerText = stringResource(R.string.profile_info_banner),
                    detailsFindText = stringResource(R.string.profile_details_text),
                    dismissDescription = stringResource(R.string.profile_dismiss_text),
                    onDismiss = onDismissBanner,
                    onDetailsFind = {
                        uriHandler.openUri("https://github.com/DipeshAggarwal/rikta-launcher")
                    }
                )
            }
        }

        if (inactiveProfileSummaries.isNotEmpty()) {
            item(key = "all_profiles_section") {
                Column {
                    SectionLabel(text = stringResource(R.string.all_profiles))
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        inactiveProfileSummaries.forEachIndexed { index, summary ->
                            ProfileListRow(
                                title = systemProfileDisplayName(summary.profile.name),
                                profileClassification = profileClassificationMap.getValue(summary.profile.id),
                                appCount = summary.appCount,
                                triggerCount = summary.triggerCount,
                                profileId = summary.profile.id,
                                iconName = summary.profile.overrides.iconName,
                                onClick = { onViewProfileDetails(summary.profile.id) }
                            )
                            if (index < inactiveProfileSummaries.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = ThemeTokens.Spacing.ExtraLarge),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }
        } else {
            item(key = "empty_profiles_state") {
                EmptyProfilesCard(
                    title = stringResource(R.string.no_inactive_profiles),
                    subtitle = stringResource(R.string.no_inactive_profiles_subtitle)
                )
            }
        }

        item(key = "create_new_profile") {
            CreateProfileRow(
                title = stringResource(R.string.create_new_profile),
                subtitle = stringResource(R.string.create_new_profile_subtitle),
                onClick = onCreateNewProfile
            )
        }
    }
}

@Composable
private fun CreateProfileRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = onClick)
                .padding(
                    horizontal = ThemeTokens.Spacing.ExtraLarge,
                    vertical = ThemeTokens.Spacing.Large
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(ThemeTokens.Icon.TertiaryIconSize)
            )
            Spacer(modifier = Modifier.width(ThemeTokens.Spacing.Large))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyProfilesCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = ThemeTokens.DefaultVerticalPadding),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = EmptyProfilesCardDefaults.InactiveElementAlpha)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = EmptyProfilesCardDefaults.TextLeftPadding,
                    vertical = EmptyProfilesCardDefaults.TextTopPadding
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Inventory2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = EmptyProfilesCardDefaults.InactiveElementAlpha),
                modifier = Modifier.size(ThemeTokens.Icon.PrimaryIconSize)
            )
            Spacer(modifier = Modifier.height(ThemeTokens.Spacing.ExtraLarge))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(ThemeTokens.Spacing.Medium))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
