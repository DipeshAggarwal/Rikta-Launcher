package com.lumina.feature.apphiding.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.lumina.feature.apphiding.R
import com.lumina.feature.apphiding.AppHidingViewModel
import com.lumina.feature.apppicker.AppPickerScreen

/**
 * Bulk selection screen for hiding apps.
 * Uses the generic [AppPickerScreen] component.
 */
@Composable
fun AppHidingScreen(
    onBack: () -> Unit,
    viewModel: AppHidingViewModel
) {
    // Collect the full list of installed apps and the current hidden set.
    val installedApps by viewModel.installedApps.collectAsState(emptyList())
    val hiddenPackages by viewModel.hiddenPackagesSet.collectAsState()

    AppPickerScreen(
        apps = installedApps,
        preSelectedApps = hiddenPackages.toList(),
        title = stringResource(R.string.manage_hidden_apps),
        onBackClicked = onBack,
        onAppClicked = { app, selected ->
            if (selected) {
                viewModel.removeHiddenApp(app.packageName)
            } else {
                viewModel.addHiddenApp(app.packageName)
            }
        }
    )
}
