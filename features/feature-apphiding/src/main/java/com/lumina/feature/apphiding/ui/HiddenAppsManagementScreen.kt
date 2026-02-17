package com.lumina.feature.apphiding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.lumina.core.ui.HapticUtils
import com.lumina.core.ui.components.settings.SettingsButton
import com.lumina.core.ui.components.settings.SettingsHeader
import com.lumina.core.ui.components.settings.SettingsSpacer
import com.lumina.core.ui.components.settings.SettingsSubheading
import com.lumina.core.ui.components.settings.SettingsSwipeableButton
import com.lumina.core.ui.components.settings.SettingsSwitch
import com.lumina.feature.apphiding.AppHidingViewModel
import com.lumina.feature.apphiding.R

/**
 * Primary management screen for hidden apps.
 * Shows a list of currently hidden apps with swipe-to-unhide functionality and global visibility settings.
 */
@Composable
fun HiddenAppsManagementScreen(
    goToBulkAppHiding: () -> Unit,
    onBack: () -> Unit,
    viewModel: AppHidingViewModel
) {
    // Observe reactive state flows from the shared ViewModel
    val hiddenApps by viewModel.hiddenApps.collectAsState()
    val showHiddenAppsInSearch by viewModel.showHiddenAppsInSearch.collectAsState()

    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SettingsHeader(onBack, stringResource(R.string.hidden_apps))
        }
        item {
            SettingsButton(
                label = stringResource(R.string.manage_hidden_apps),
                isTopOfGroup = true,
                onClick = {
                    goToBulkAppHiding()
                }
            )
        }

        item {
            SettingsSwitch(
                label = stringResource(R.string.show_hidden_apps_in_search),
                checked = showHiddenAppsInSearch,
                onCheckedChange = { viewModel.setShowHiddenAppsInSearch(it) },
                isBottomOfGroup = true
            )
        }

        item {
            SettingsSubheading(stringResource(R.string.swipe_to_show_app))
        }

        items(
            items = hiddenApps,
            key = { app -> app.packageName }
        ) { app ->
            SettingsSwipeableButton(
                modifier = Modifier.animateItem(), // Smoothly handles removal animations.
                label = app.displayName,
                onClick = {
                    viewModel.launchApp(context, app.packageName)
                },
                onDeleteClick = {
                    HapticUtils.performHapticFeedback(haptics)
                    viewModel.removeHiddenApp(app.packageName)
                },
                isTopOfGroup = hiddenApps.firstOrNull() == app,
                isBottomOfGroup = hiddenApps.lastOrNull() == app
            )
        }

        item { SettingsSpacer() }
        item { SettingsSpacer() }
    }
}
