package com.lumina.feature.settings.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lumina.core.ui.Motion.SCREEN_TRANSITION_MS
import com.lumina.feature.settings.SettingsViewModel

const val SPACER_CONFIG_ROUTE  = "settings/spacer_config"

fun NavGraphBuilder.settingsNavigation(
    navController: NavController
) {
    composable(
        route = SPACER_CONFIG_ROUTE,
        enterTransition = { fadeIn(tween(SCREEN_TRANSITION_MS)) },
        exitTransition = { fadeOut(tween(SCREEN_TRANSITION_MS)) }
    ) { navBackStackEntry ->
        val viewModel: SettingsViewModel = hiltViewModel(navBackStackEntry)

        SpacerConfigScreen(
            { navController.popBackStack() },
            viewModel
        )
    }
}
