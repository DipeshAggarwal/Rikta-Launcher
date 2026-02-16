package com.lumina.feature.apphiding.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lumina.feature.apphiding.R
import com.lumina.feature.apphiding.AppHidingViewModel
import com.lumina.feature.apppicker.AppPickerScreen

@Composable
fun AppHidingScreen(
    launcherPackageName: String,
    onBack: () -> Unit,
    viewModel: AppHidingViewModel = hiltViewModel()
) {
    val installedApps by viewModel.installedApps.collectAsState(emptyList())
    val hiddenApps by viewModel.hiddenApps.collectAsState()

    AppPickerScreen(
        apps = installedApps,
        launcherPackageName = launcherPackageName,
        preSelectedApps = hiddenApps.map { it.packageName }.toSet(),
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
