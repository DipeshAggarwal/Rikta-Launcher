package com.lumina.feature.profiles.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lumina.core.ui.components.StandardListScaffold
import com.lumina.domain.profiles.model.LauncherProfilePermissions
import com.lumina.domain.profiles.model.LauncherProfileRestrictions
import com.lumina.domain.profiles.model.LauncherProfileSettings
import com.lumina.feature.profiles.R
import com.lumina.feature.profiles.ui.bottomsheet.AutoAppCategoryBottomSheet
import com.lumina.feature.profiles.ui.component.AccordionContent
import com.lumina.feature.profiles.ui.model.AccordionRow

@Composable
fun ProfileRulesScreen(
    permissions: LauncherProfilePermissions,
    restrictions: LauncherProfileRestrictions,
    settings: LauncherProfileSettings,
    onPermissionsChange: ((LauncherProfilePermissions) -> LauncherProfilePermissions) -> Unit,
    onRestrictionsChange: ((LauncherProfileRestrictions) -> LauncherProfileRestrictions) -> Unit,
    onSettingsChange: ((LauncherProfileSettings) -> LauncherProfileSettings) -> Unit,
    onBack: () -> Unit
) {
    StandardListScaffold(
        title = stringResource(R.string.profile_rules_header),
        onBack = onBack
    ) {
        item(key = "profile_rules_subtitle") {
            Text(
                text = stringResource(R.string.profile_rules_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item(key = "profile_rules_app_access") {
            ProfileRulesAppAccessScreen(
                settings = settings,
                restrictions = restrictions,
                onRestrictionsChange = onRestrictionsChange,
                onSettingsChange = onSettingsChange
            )
        }

        item(key = "profile_rules_notifications") {
            ProfileRulesNotificationScreen(
                restrictions = restrictions,
                onRestrictionsChange = onRestrictionsChange
            )
        }
    }
}

@Composable
fun ProfileRulesAppAccessScreen(
    settings: LauncherProfileSettings,
    restrictions: LauncherProfileRestrictions,
    onRestrictionsChange: ((LauncherProfileRestrictions) -> LauncherProfileRestrictions) -> Unit,
    onSettingsChange: ((LauncherProfileSettings) -> LauncherProfileSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddCategorySheet by remember { mutableStateOf(false) }

    val rows = listOf(
        AccordionRow.Switch(
            title = stringResource(R.string.profile_rules_hide_unauthorized_apps),
            subtitle = stringResource(R.string.profile_rules_hide_unauthorized_apps_subtitle),
            checked = restrictions.blockUnauthorisedApps,
            onCheckedChange = { isChecked ->
                onRestrictionsChange { current -> current.copy(blockUnauthorisedApps = isChecked) }
            }
        ),
        AccordionRow.Switch(
            title = stringResource(R.string.profile_rules_hide_apps_list),
            subtitle = stringResource(R.string.profile_rules_hide_apps_list_subtitle),
            checked = restrictions.blockAppList,
            onCheckedChange = { isChecked ->
                onRestrictionsChange { current -> current.copy(blockAppList = isChecked) }
            }
        ),
        AccordionRow.Switch(
            title = stringResource(R.string.profile_rules_block_system_app),
            subtitle = stringResource(R.string.profile_rules_block_system_app_subtitle),
            checked = restrictions.blockSystemAppAdd,
            onCheckedChange = { isChecked ->
                onRestrictionsChange { current -> current.copy(blockSystemAppAdd = isChecked) }
            }
        ),
        AccordionRow.Detail(
            title = stringResource(R.string.profile_rules_auto_add_categories),
            subtitle = stringResource(R.string.profile_rules_auto_add_categories_subtitle),
            onClick = { showAddCategorySheet = true },
        )
    )

    AccordionContent(
        title = stringResource(R.string.profile_rules_app_access_heading),
        subtitle = stringResource(R.string.profile_rules_app_access_subtitle),
        rows = rows,
        modifier = modifier
    )

    if (showAddCategorySheet) {
        AutoAppCategoryBottomSheet(
            initialSelectedCategories = settings.autoAddCategoryApps.toSet(),
            onDismiss = { updatedCategories ->
                showAddCategorySheet = false
                onSettingsChange { currentSettings ->
                    currentSettings.copy(autoAddCategoryApps = updatedCategories.toList())
                }
            },
        )
    }
}

@Composable
fun ProfileRulesNotificationScreen(
    restrictions: LauncherProfileRestrictions,
    onRestrictionsChange: ((LauncherProfileRestrictions) -> LauncherProfileRestrictions) -> Unit,
    modifier: Modifier = Modifier
) {

    val rows = listOf(
        AccordionRow.Switch(
            title = stringResource(R.string.profile_rules_filter_notifications),
            subtitle = stringResource(R.string.profile_rules_profile_rules_filter_notifications_subtitle),
            checked = restrictions.filterNotifications,
            onCheckedChange = { isChecked ->
                onRestrictionsChange { current -> current.copy(filterNotifications = isChecked) }
            }
        )
    )

    AccordionContent(
        title = stringResource(R.string.profile_rules_notification_heading),
        subtitle = stringResource(R.string.profile_rules_notification_subtitle),
        rows = rows,
        modifier = modifier
    )
}
