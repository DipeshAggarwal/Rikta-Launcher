package com.lumina.feature.home.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lumina.core.ui.Motion.SCREEN_TRANSITION_DURATION
import com.lumina.feature.home.HomeViewModel

const val HOME_ROUTE = "home_screen"

fun NavGraphBuilder.homeNavigation(
    onNavigateToSettings: () -> Unit
) {
    composable(
        route = HOME_ROUTE,
        enterTransition = { fadeIn(tween(SCREEN_TRANSITION_DURATION)) },
        exitTransition = { fadeOut(tween(SCREEN_TRANSITION_DURATION)) }
    ) {
        val viewModel: HomeViewModel = hiltViewModel()
        HomeScreen(
            viewModel = viewModel,
            onNavigateToSettings = onNavigateToSettings
        )
    }
}
