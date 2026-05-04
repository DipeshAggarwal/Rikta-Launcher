package com.lumina.feature.home.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.lumina.core.ui.components.home.BottomSheet
import com.lumina.core.ui.components.home.BottomSheetAppAction
import com.lumina.core.ui.components.home.BottomSheetViewData
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
    var currentMenuState by remember { mutableStateOf(AppMenuState.MAIN) }

    when (state) {
        is BottomSheetState.None -> Unit
        is BottomSheetState.PrivateSpaceSettings -> {}
        is BottomSheetState.ShortcutOptions -> {
            val viewData = BottomSheetViewData(
                title = state.shortcut.label,
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
            BottomSheet(
                viewData = viewData,
                onDismissRequest = { viewModel.onBottomSheetDismissed() },
                sheetState = sheetState,
            )
        }
        is BottomSheetState.AppOptions -> {
            val app = state.selectedApp.app
            val appInfo = app.info

            val viewData = when (currentMenuState) {
                AppMenuState.MAIN -> BottomSheetViewData(
                    title = appInfo.displayName,
                    shortcutActions = state.shortcuts.map { shortcut ->
                        BottomSheetAppAction(
                            label = shortcut.shortLabel,
                            onClick = { viewModel.onLaunchShortcut(shortcut) }
                        )
                    },
                    quickActions = listOf(
                        BottomSheetAppAction(
                            label = if (state.selectedApp.isFavourite)
                                stringResource(R.string.rem_from_fav)
                            else
                                stringResource(R.string.add_to_fav),
                            icon = if (state.selectedApp.isFavourite)
                                Icons.Outlined.Favorite
                            else
                                Icons.Outlined.FavoriteBorder,
                            onClick = { viewModel.onToggleFavourite(app, false) }
                        ),
                        BottomSheetAppAction(
                            label = stringResource(R.string.app_info),
                            icon = Icons.Outlined.Info,
                            onClick = { viewModel.onOpenAppInfo(appInfo) }
                        ),
                        BottomSheetAppAction(
                            label = stringResource(R.string.uninstall),
                            icon = Icons.Outlined.Delete,
                            onClick = { viewModel.onUninstallApp(appInfo) }
                        )
                    ),
                    actions = listOf(
                        BottomSheetAppAction(
                            label = stringResource(R.string.organise_app),
                            trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            onClick = { currentMenuState = AppMenuState.ORGANISE }
                        ),
                        BottomSheetAppAction(
                            label = stringResource(R.string.wellbeing),
                            trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            onClick = { currentMenuState = AppMenuState.WELLBEING }
                        )
                    )
                )

                AppMenuState.ORGANISE -> BottomSheetViewData(
                    title = stringResource(R.string.organise_app_header, appInfo.displayName),
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    onHeaderClick = { currentMenuState = AppMenuState.MAIN },
                    actions = listOf(
                        BottomSheetAppAction(
                            label = stringResource(R.string.rename_app),
                            onClick = {}
                        ),
                        BottomSheetAppAction(
                            label = stringResource(R.string.change_category),
                            onClick = {}
                        ),
                        BottomSheetAppAction(
                            label = stringResource(R.string.add_to_profile),
                            onClick = {}
                        )
                    )
                )

                AppMenuState.WELLBEING -> BottomSheetViewData(
                    title = stringResource(R.string.wellbeing_header, appInfo.displayName),
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    onHeaderClick = { currentMenuState = AppMenuState.MAIN },
                    actions = listOf(
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
                        )
                    )
                )
            }

            BottomSheet(
                viewData = viewData,
                sheetState = sheetState,
                onDismissRequest = {
                    currentMenuState = AppMenuState.MAIN
                    viewModel.onBottomSheetDismissed()
                }
            )
        }
    }
}

enum class AppMenuState {
    MAIN,
    ORGANISE,
    WELLBEING
}
