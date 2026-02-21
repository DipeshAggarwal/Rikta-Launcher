package com.lumina.feature.home.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumina.feature.home.model.HomePage
import com.lumina.feature.home.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val screenTimePageVisible by viewModel.screenTimePageVisible.collectAsStateWithLifecycle()
    val appsListScrollState = rememberLazyListState()

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
}
