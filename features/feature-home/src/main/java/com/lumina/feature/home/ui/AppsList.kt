package com.lumina.feature.home.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumina.feature.home.HomeViewModel

@Composable
fun AppList(
    viewModel: HomeViewModel,
    scrollState: LazyListState
) {
    val haptics = LocalHapticFeedback.current
    val uiState by viewModel.homeUiState.collectAsStateWithLifecycle()
}
