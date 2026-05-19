package com.lumina.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.lumina.core.ui.ThemeTokens

@Composable
fun StandardListScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    listModifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    verticalColumnSpacing: Dp = ThemeTokens.Spacing.ExtraLarge,
    lazyListState: LazyListState = rememberLazyListState(),
    content: LazyListScope.() -> Unit
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = {
            snackbarHostState?.let { SnackbarHost(it) }
        },
        topBar = {
            TopTitleBar(
                title = title,
                onBack = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .then(listModifier),
            contentPadding = PaddingValues(
                horizontal = ThemeTokens.Spacing.Small,
                vertical = ThemeTokens.Spacing.Large
            ),
            verticalArrangement = Arrangement.spacedBy(verticalColumnSpacing)
        ) {
            content()
        }
    }
}
