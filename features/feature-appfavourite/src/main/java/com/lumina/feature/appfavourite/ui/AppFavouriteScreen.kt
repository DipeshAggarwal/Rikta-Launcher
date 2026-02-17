package com.lumina.feature.appfavourite.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.lumina.feature.appfavourite.AppFavouriteViewModel
import com.lumina.feature.appfavourite.R
import com.lumina.feature.apppicker.AppPickerScreen

@Composable
fun AppFavouriteScreen(
    onBack: () -> Unit,
    viewModel: AppFavouriteViewModel
) {
    // Collect the full list of installed apps and the current favourite set.
    val installedApps by viewModel.installedApps.collectAsState(emptyList())
    val favouritePackages by viewModel.favouritePackages.collectAsState()

    AppPickerScreen(
        apps = installedApps,
        preSelectedApps = favouritePackages,
        title = stringResource(R.string.manage_favourite_apps_title),
        reorderable = true,
        onAppMoved = { fromIndex, toIndex ->
            viewModel.reorderFavouriteApps(fromIndex, toIndex)
        },
        onBackClicked = onBack,
        onAppClicked = { app, selected ->
            if (selected) {
                viewModel.removeFavouriteApp(app.packageName)
            } else {
                viewModel.addFavouriteApp(app.packageName)
            }
        }
    )
}
