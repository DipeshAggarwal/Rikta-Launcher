package com.lumina.feature.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumina.core.ui.components.home.AnimatedPillSearchBar
import com.lumina.domain.settings.AppsAlignment
import com.lumina.feature.home.HomeViewModel
import com.lumina.feature.home.model.HomeUiState

private fun AppsAlignment.toAlignment(): Alignment.Horizontal = when(this) {
    AppsAlignment.Start -> Alignment.Start
    AppsAlignment.Center -> Alignment.CenterHorizontally
    AppsAlignment.End -> Alignment.End
}

@Composable
fun AppList(
    viewModel: HomeViewModel,
    scrollState: LazyListState
) {
    val haptics = LocalHapticFeedback.current
    val uiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    Box(
        Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp, 0.dp),
//            horizontalAlignment =
        ) { }
    }
}

@Composable
private fun SearchBox(
    uiState: HomeUiState.Ready,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSearchTextChanged: (String) -> Unit,
    onSearchDone: (String) -> Unit,
    autoFocus: Boolean
) {
    AnimatedPillSearchBar(
        isExpanded = isExpanded,
        onExpandedChange = { onExpandedChange(it) },
        onSearchTextChanged = { onSearchTextChanged(it) },
        onSearchDone = { onSearchDone(it) },
        modifier = Modifier,
        initialText = uiState.searchQuery,
        autoFocus = autoFocus
    )
}
