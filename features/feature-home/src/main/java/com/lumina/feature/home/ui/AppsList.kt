package com.lumina.feature.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumina.core.ui.components.home.AnimatedPillSearchBar
import com.lumina.core.ui.components.home.AppListItem
import com.lumina.core.ui.components.home.AppsListHeader
import com.lumina.core.ui.components.settings.SettingsSpacer
import com.lumina.feature.home.HomeViewModel
import com.lumina.feature.home.model.HomeUiState

private object AppsListDefaults {
    val HorizontalPadding = 30.dp
    val SearchBoxSpacing = 15.dp
    val BottomSearchVerticalPadding = 25.dp
}

@Composable
fun AppsList(
    viewModel: HomeViewModel,
    scrollState: LazyListState
) {
    val haptics = LocalHapticFeedback.current
    val uiState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val appsListSettings by viewModel.appListSettings.collectAsStateWithLifecycle()
    val isSearchExpanded by viewModel.isSearchExpanded.collectAsStateWithLifecycle()

    val readyState = uiState as? HomeUiState.Ready ?: return

    Box(
        Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppsListDefaults.HorizontalPadding),
            horizontalAlignment = appsListSettings.appsListAlignment.toAlignment()
        ) {
            item {
                AppsListHeader()
            }

            item {
                if (appsListSettings.showSearchBox && !appsListSettings.showSearchBoxAtBottom) {
                    Spacer(modifier = Modifier.height(AppsListDefaults.SearchBoxSpacing))
                    SearchBox(
                        uiState = readyState,
                        isExpanded = isSearchExpanded,
                        onExpandedChange = { viewModel.setSearchExpanded(it) },
                        onSearchTextChanged = { viewModel.onSearchQueryChanged(it) },
                        onSearchDone = { viewModel.onSearchDone() },
                        autoFocus = appsListSettings.autoFocusSearch
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }

            items(readyState.apps, key = { app -> app.packageName }) { app ->
                AppListItem(
                    appName = app.displayName,
                    screenTime = null,
                    showScreenTime = false,
                    onAppClick = { viewModel.onAppOpened(app) },
                    onAppLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onAppLongPressed(app)
                    },
                    alignment = appsListSettings.appsListAlignment.toAlignment()
                )
            }

            // TODO: Private space
            // TODO: Work apps
        }

        Column(
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .padding(
                    horizontal = AppsListDefaults.HorizontalPadding,
                    vertical = AppsListDefaults.BottomSearchVerticalPadding)
                .fillMaxWidth(),
            horizontalAlignment = appsListSettings.appsListAlignment.toAlignment()
        ) {
            if (appsListSettings.showSearchBox && appsListSettings.showSearchBoxAtBottom) {
                Spacer(modifier = Modifier.height(AppsListDefaults.SearchBoxSpacing))
                SearchBox(
                    uiState = readyState,
                    isExpanded = isSearchExpanded,
                    onExpandedChange = { viewModel.setSearchExpanded(it) },
                    onSearchTextChanged = { viewModel.onSearchQueryChanged(it) },
                    onSearchDone = { viewModel.onSearchDone() },
                    autoFocus = appsListSettings.autoFocusSearch
                )
                SettingsSpacer()
            }
        }
    }
}

@Composable
private fun SearchBox(
    uiState: HomeUiState.Ready,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSearchTextChanged: (String) -> Unit,
    onSearchDone: () -> Unit,
    autoFocus: Boolean
) {
    AnimatedPillSearchBar(
        isExpanded = isExpanded,
        onExpandedChange = { onExpandedChange(it) },
        onSearchTextChanged = { onSearchTextChanged(it) },
        onSearchDone = { onSearchDone() },
        modifier = Modifier,
        initialText = uiState.searchQuery,
        autoFocus = autoFocus
    )
}
