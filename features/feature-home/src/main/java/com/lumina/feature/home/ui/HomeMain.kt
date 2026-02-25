package com.lumina.feature.home.ui

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.HapticUtils
import com.lumina.core.ui.components.home.AppListItem
import com.lumina.core.ui.components.home.Clock
import com.lumina.core.ui.components.home.Date
import com.lumina.domain.settings.HomeSettings
import com.lumina.feature.home.HomeViewModel
import com.lumina.feature.home.model.HomeUiState

private const val PULL_DOWN_REGISTER_THRESHOLD = 50f

private object HomeMainDefaults {
    val EdgeSpacing = 90.dp
    val HorizontalPadding = 30.dp
    val SectionSpacing = 10.dp
    val FirstTimeHelpSpacing = 15.dp
}

@Composable
fun HomeMain(
    viewModel: HomeViewModel,
    uiState: HomeUiState,
    homeSettings: HomeSettings,
    scrollState: LazyListState
) {
    val haptics  = LocalHapticFeedback.current
    val readyState = uiState as? HomeUiState.Ready

    val nestedScrollConnection = remember {
        object: NestedScrollConnection {
            var totalDrag = 0f

            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.UserInput && available.y > 0) {
                    totalDrag += available.y

                    if (totalDrag > PULL_DOWN_REGISTER_THRESHOLD) {
                        viewModel.onExpandNotificationShade()
                        totalDrag = 0f
                    }
                } else {
                    totalDrag = 0f
                }
                return Offset.Zero
            }
        }
    }

    LazyColumn(
        state = scrollState,
        verticalArrangement = homeSettings.homeVerticalAlignment.toAlignment(),
        horizontalAlignment = homeSettings.homeAlignment.toAlignment(),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = HomeMainDefaults.HorizontalPadding)
            .nestedScroll(nestedScrollConnection)
    ) {
        item { Spacer(Modifier.height(HomeMainDefaults.HorizontalPadding)) }

        item {
            if (homeSettings.showClock) {
                Clock(
                    bigClock = homeSettings.showBigClock,
                    twelveHourDisplay = homeSettings.useTwelveHourDisplay,
                    onClockClick = { viewModel.openAlarm() },
                    homeAlignment = homeSettings.homeAlignment.toAlignment()
                )
            }
        }

        item {
            if (homeSettings.showDate) {
                FlowRow {
                    Date(
                        onDateClick = { viewModel.openCalendar() },
                        homeAlignment = homeSettings.homeAlignment.toAlignment(),
                        small = homeSettings.showBigDate
                    )
                }

                // TODO: Screen Time
                // TODO: Weather
            }
        }

        item { Spacer(Modifier.height(HomeMainDefaults.SectionSpacing)) }

        // TODO: Widgets
        if (readyState != null) {
            items(
                readyState.favourites,
                key = { "${it.packageName}_${it.componentClassName}_${it.userHandleNumber}" }
            ) { app ->
                AppListItem(
                    appName = app.displayName,
                    screenTime = null,
                    showScreenTime = false,
                    onAppClick = { viewModel.onAppOpened(app) },
                    onAppLongClick = {
                        HapticUtils.performHapticFeedback(haptics)
                        viewModel.onAppLongPressed(app)
                    },
                    alignment = homeSettings.homeAlignment.toAlignment()
                )
            }
        }

        if (homeSettings.showFirstTimeHelp) {
            item { Spacer(Modifier.height(HomeMainDefaults.FirstTimeHelpSpacing)) }
            item { FirstTimeHelp() }
        }

        item { Spacer(Modifier.height(HomeMainDefaults.EdgeSpacing)) }
    }
}
