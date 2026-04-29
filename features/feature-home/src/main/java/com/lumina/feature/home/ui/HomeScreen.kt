package com.lumina.feature.home.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumina.core.ui.HapticUtils.performHapticFeedback
import com.lumina.feature.home.model.HomePage
import com.lumina.feature.home.HomeViewModel
import com.lumina.feature.home.model.HomeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val homeSettings by viewModel.homeSettings.collectAsStateWithLifecycle()
    val bottomSheetState by viewModel.bottomSheetState.collectAsStateWithLifecycle()

    val screenTimePageVisible = homeSettings.showScreenTimePage
    val homeMainScrollState = rememberLazyListState()
    val appsListScrollState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState()

    val haptics  = LocalHapticFeedback.current

    val pages = remember(screenTimePageVisible) {
        buildList {
            if (screenTimePageVisible) add(HomePage.ScreenTime)
            add(HomePage.Main)
            add(HomePage.Apps)
        }
    }

    val pagerState = rememberPagerState(
        initialPage = pages.indexOf(HomePage.Main),
        pageCount = { pages.size }
    )

    LaunchedEffect(viewModel, pagerState) {
        viewModel.navigateHomeEvent.collect {
            pagerState.animateScrollToPage(pages.indexOf(HomePage.Main))
            appsListScrollState.scrollToItem(0)
        }
    }

    LaunchedEffect(screenTimePageVisible) {
        val mainPageIndex = pages.indexOf(HomePage.Main)

        if (pagerState.currentPage >= pages.size) {
            pagerState.scrollToPage(mainPageIndex)
        }
    }

    when (val state = uiState) {
        is HomeUiState.Loading -> {}
        is HomeUiState.Error -> {}
        is HomeUiState.Ready -> {
            HomeBottomSheet(
                state = bottomSheetState,
                sheetState = sheetState,
                viewModel = viewModel
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                performHapticFeedback(haptics)
                                onNavigateToSettings()
                            }
                        )
                    },
                beyondViewportPageCount = 1
            ) { pageIndex ->
                when (pages[pageIndex]) {
                    HomePage.ScreenTime -> {}
                    HomePage.Main -> {
                        HomeMain(
                            viewModel = viewModel,
                            uiState = state,
                            homeSettings = homeSettings,
                            scrollState = homeMainScrollState
                        )
                    }
                    HomePage.Apps -> { AppsList(viewModel, appsListScrollState)}
                }
            }
        }
    }
}
