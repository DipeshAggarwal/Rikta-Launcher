package com.lumina.feature.appcountdown.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.lumina.feature.appcountdown.CountdownSettingsViewModel
import com.lumina.feature.appcountdown.R
import com.lumina.core.ui.screens.AppPickerScreen

@Composable
fun AppCountdownScreen(
    onBack: () -> Unit,
    viewModel: CountdownSettingsViewModel
) {
    val installedApps by viewModel.activeProfileApps.collectAsState(emptyList())
    val countdownPackages by viewModel.countdownAppsSet.collectAsState()

    AppPickerScreen(
        apps = installedApps,
        preSelectedApps = countdownPackages.toList(),
        title = stringResource(R.string.manage_countdown_apps),
        activeTitle = stringResource(R.string.countdown_apps_header),
        onBackClicked = onBack,
        onAppClicked = { app, selected ->
            if (selected) {
                viewModel.removeCountdownApp(app)
            } else {
                viewModel.addCountdownApp(app)
            }
        }
    )
}
