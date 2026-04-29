package com.lumina.feature.home.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.lumina.core.ui.components.home.BottomSheet
import com.lumina.core.ui.components.home.BottomSheetAppAction
import com.lumina.feature.home.HomeViewModel
import com.lumina.feature.home.R
import com.lumina.feature.home.model.BottomSheetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBottomSheet(
    state: BottomSheetState,
    sheetState: SheetState,
    viewModel: HomeViewModel
) {
    when (state) {
        is BottomSheetState.None -> Unit
        is BottomSheetState.PrivateSpaceSettings -> {}
        is BottomSheetState.ShortcutOptions -> {
            BottomSheet(
                title = state.shortcut.label,
                sheetState = sheetState,
                onDismissRequest = { viewModel.onBottomSheetDismissed() },
                shortcutActions = emptyList(),
                actions = listOf(
                    BottomSheetAppAction(
                        label = if (state.isFavourite) {
                            stringResource(R.string.rem_from_fav)
                        } else {
                            stringResource(R.string.add_to_fav)
                        },
                        onClick = { viewModel.onToggleFavourite(state.shortcut) }
                    ),
                    BottomSheetAppAction(
                        label =  stringResource(R.string.delete_shortcut),
                        onClick = { viewModel.onDeleteShortcut(state.shortcut) }
                    )
                )
            )
        }
        is BottomSheetState.AppOptions -> {
            val app = state.selectedApp.app
            val appInfo = app.info

            BottomSheet(
                title = appInfo.displayName,
                sheetState = sheetState,
                onDismissRequest = { viewModel.onBottomSheetDismissed() },
                shortcutActions = state.shortcuts.map { shortcut ->
                    BottomSheetAppAction(
                        label = shortcut.shortLabel,
                        onClick = { viewModel.onLaunchShortcut(shortcut) }
                    )
                },
                actions = listOf(
                    BottomSheetAppAction(
                        label = if (state.selectedApp.isFavourite) {
                            stringResource(R.string.rem_from_fav)
                        } else {
                            stringResource(R.string.add_to_fav)
                        },
                        onClick = { viewModel.onToggleFavourite(app) }
                    ),
                    BottomSheetAppAction(
                        label = if (state.selectedApp.isCountdownRequired) {
                            stringResource(R.string.remove_countdown)
                        } else {
                            stringResource(R.string.add_countdown)
                        },
                        onClick = { viewModel.onToggleCountdown(app) }
                    ),
                    BottomSheetAppAction(
                        label = stringResource(R.string.hide),
                        onClick = { viewModel.onHideApp(appInfo.packageName) }
                    ),
                    BottomSheetAppAction(
                        label = stringResource(R.string.app_info),
                        onClick = { viewModel.onOpenAppInfo(appInfo) }
                    ),
                    BottomSheetAppAction(
                        label = stringResource(R.string.uninstall),
                        onClick = { viewModel.onUninstallApp(appInfo) }
                    )
                )
            )
        }
    }
}
