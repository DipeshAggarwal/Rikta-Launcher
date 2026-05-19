package com.lumina.feature.profiles.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumina.core.model.ProfileAuthMethod
import com.lumina.core.ui.ThemeTokens
import com.lumina.core.ui.components.ConfirmationBottomSheet
import com.lumina.core.ui.components.DestructiveActionButton
import com.lumina.core.ui.components.DetailRow
import com.lumina.core.ui.components.OutlinedActionButton
import com.lumina.core.ui.components.StandardListScaffold
import com.lumina.core.ui.extensions.systemProfileDisplayName
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.feature.profiles.ProfileDetailEvent
import com.lumina.feature.profiles.ProfileDetailViewModel
import com.lumina.feature.profiles.R
import com.lumina.feature.profiles.ui.component.ProfileDetailCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(
    viewModel: ProfileDetailViewModel = hiltViewModel(),
    onNavigateToSummary: () -> Unit,
    onNavigateToHomeScreen: () -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToAllowedApps: () -> Unit,
    onNavigateToTrigger: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onProfileDuplicated: (String) -> Unit,
    onProfileDeleted: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDuplicateSheet by remember { mutableStateOf(false) }
    var showDeleteSheet by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileDetailEvent.ProfileDeleted -> onProfileDeleted()
                is ProfileDetailEvent.ProfileDuplicated -> onProfileDuplicated(event.newProfileId)
                is ProfileDetailEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val profile = checkNotNull(uiState.profile)
    val resolvedName = systemProfileDisplayName(profile.name)
    val profileClassification = checkNotNull(uiState.classification)

    StandardListScaffold(
        title = resolvedName,
        onBack = onBack,
        snackbarHostState = snackbarHostState
    ) {
        item(key = "viewing_profile_card") {
            ProfileDetailCard(
                title = resolvedName,
                profileClassification = profileClassification,
                appCount = uiState.allowedAppCount,
                triggerCount = uiState.triggerCount,
                profileId = profile.id,
                iconName = profile.overrides.iconName,
                activeText = if (uiState.isActive) stringResource(R.string.active_now) else null,
                switchText = stringResource(
                    R.string.profile_details_make_active,
                    resolvedName
                ),
                onSwitch = { viewModel.toggleActiveState() }
            )
        }

        item(key = "viewing_profile_settings_section") {
            SettingsSectionCard(
                profile = profile,
                allowedAppCount = uiState.allowedAppCount,
                triggerCount = uiState.triggerCount,
                onNavigateToSummary = onNavigateToSummary,
                onNavigateToHomeScreen = onNavigateToHomeScreen,
                onNavigateToAllowedApps = onNavigateToAllowedApps,
                onNavigateToRestrictions = onNavigateToRules,
                onNavigateToTrigger = onNavigateToTrigger,
                onNavigateToAppearance = onNavigateToAppearance,
                onNavigateToSecurity = onNavigateToSecurity
            )
        }

        item(key = "viewing_profile_manage_action") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ThemeTokens.Spacing.Small)
            ) {
                OutlinedActionButton(
                    text = stringResource(
                        R.string.profile_details_duplicate_profile,
                        resolvedName
                    ),
                    enabled = !uiState.isPerformingAction,
                    onClick = { showDuplicateSheet = true }
                )
                DestructiveActionButton(
                    text = stringResource(
                        R.string.profile_details_delete_profile,
                        resolvedName
                    ),
                    enabled = !uiState.isPerformingAction && !profile.isAdmin,
                    onClick = { showDeleteSheet = true }
                )
            }
        }
    }

    if (showDuplicateSheet) {
        ConfirmationBottomSheet(
            icon = Icons.Outlined.ContentCopy,
            title = stringResource(
                R.string.profile_details_duplicate_profile,
                resolvedName
            ),
            subtitle = stringResource(R.string.profile_details_duplicate_profile_subtitle),
            confirmText = stringResource(R.string.profile_details_duplicate_button_text),
            confirmColor = MaterialTheme.colorScheme.primary,
            cancelText = stringResource(R.string.profile_details_cancel_button_text),
            onConfirm = {
                showDuplicateSheet = false
                viewModel.duplicateProfile(resolvedName)
            },
            onDismiss = { showDuplicateSheet = false }
        )
    }

    if (showDeleteSheet) {
        ConfirmationBottomSheet(
            icon = Icons.Outlined.Delete,
            title = stringResource(
                R.string.profile_details_delete_profile,
                resolvedName
            ),
            subtitle = stringResource(R.string.profile_details_delete_profile_subtitle),
            confirmText = stringResource(R.string.profile_details_delete_button_text),
            confirmColor = MaterialTheme.colorScheme.primary,
            cancelText = stringResource(R.string.profile_details_cancel_button_text),
            onConfirm = {
                showDeleteSheet = false
                viewModel.deleteProfile()
            },
            onDismiss = { showDeleteSheet = false }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    profile: LauncherProfile,
    allowedAppCount: Int,
    triggerCount: Int,
    onNavigateToSummary: () -> Unit,
    onNavigateToHomeScreen: () -> Unit,
    onNavigateToAllowedApps: () -> Unit,
    onNavigateToRestrictions: () -> Unit,
    onNavigateToTrigger: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    modifier: Modifier = Modifier
) {
    val apperanceSummary = profile.overrides.theme ?: stringResource(R.string.profile_details_default_theme)

    val enabledRestrictionsCount = with(profile.restrictions) {
        listOf(
            blockAppList,
            blockProfileTriggerSwitching,
            blockUnauthorisedApps,
            blockRecentApps,
            blockSystemAppAdd,
            blockNotificationShade,
            filterNotifications,
            switchOnDeviceLock
        ).count { it }
    }
    val securitySummary = when {
        profile.auth.activationKey != null && profile.auth.entryAuthMethod != ProfileAuthMethod.NONE ->
            stringResource(R.string.profile_details_biometric_and_pin)
        profile.auth.activationKey != null -> stringResource(R.string.profile_details_security_key)
        profile.auth.entryAuthMethod != ProfileAuthMethod.NONE ->
            stringResource(R.string.profile_details_biometric_required)
        else -> stringResource(R.string.profile_details_no_security)
    }

    val rows = listOf(
        SettingsRowData(
            label = stringResource(R.string.profile_details_summary),
            subtitle = null,
            onClick = onNavigateToSummary
        ),
        SettingsRowData(
            label = stringResource(R.string.profile_details_home_screen),
            subtitle = null,
            onClick = onNavigateToHomeScreen
        ),
        SettingsRowData(
            label = stringResource(R.string.profile_details_allowed_apps),
            subtitle = stringResource(R.string.profile_details_allowed_apps_count, allowedAppCount),
            onClick = onNavigateToAllowedApps
        ),
        SettingsRowData(
            label = stringResource(R.string.profile_details_rules),
            subtitle = stringResource(R.string.profile_details_rules_count, enabledRestrictionsCount),
            onClick = onNavigateToRestrictions
        ),
        SettingsRowData(
            label = stringResource(R.string.profile_details_triggers),
            subtitle = stringResource(R.string.profile_details_triggers_count, triggerCount),
            onClick = onNavigateToTrigger
        ),
        SettingsRowData(
            label = stringResource(R.string.profile_details_appearance),
            subtitle = apperanceSummary,
            onClick = onNavigateToAppearance
        ),
        SettingsRowData(
            label = stringResource(R.string.profile_details_security),
            subtitle = securitySummary,
            onClick = onNavigateToSecurity
        )
    )

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        rows.forEachIndexed { index, row ->
            DetailRow(
                label = row.label,
                subtitle = row.subtitle,
                onClick = row.onClick
            )
            if (index < rows.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = ThemeTokens.Spacing.ExtraLarge),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

private class SettingsRowData(
    val label: String,
    val subtitle: String?,
    val onClick: () -> Unit
)
