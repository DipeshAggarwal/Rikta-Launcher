package com.lumina.feature.appcountdown.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lumina.core.ui.Motion.SCREEN_TRANSITION_MS
import com.lumina.feature.appcountdown.CountdownSettingsViewModel

const val APPS_COUNTDOWN_MANAGEMENT_ROUTE  = "countdown_apps_manage"
const val APPS_COUNTDOWN_SETTINGS_ROUTE  = "countdown_apps_settings"
const val APP_COUNTDOWN_APP_PICKER_ROUTE = "countdown_app_picker"

fun NavGraphBuilder.appCountdownNavigation(
    navController: NavController
) {
    composable(
        route = APPS_COUNTDOWN_MANAGEMENT_ROUTE,
        enterTransition = { fadeIn(tween(SCREEN_TRANSITION_MS)) },
        exitTransition = { fadeOut(tween(SCREEN_TRANSITION_MS)) }
    ) { navBackStackEntry ->
        val viewModel: CountdownSettingsViewModel = hiltViewModel(navBackStackEntry)

        AppCountdownManagementScreen(
            { navController.navigate(APPS_COUNTDOWN_SETTINGS_ROUTE) },
            { navController.navigate(APP_COUNTDOWN_APP_PICKER_ROUTE) },
            { navController.popBackStack() },
            viewModel
        )
    }

    composable(
        APPS_COUNTDOWN_SETTINGS_ROUTE,
        enterTransition = { fadeIn(tween(SCREEN_TRANSITION_MS)) },
        exitTransition = { fadeOut(tween(SCREEN_TRANSITION_MS)) }
    ) { navBackStackEntry ->
        // remember is needed so that reference remains consistent across recompositions
        // Also, `getBackStackEntry` is an expensive operation and doesn't change.
        val parentEntry = remember(navBackStackEntry) {
            navController.getBackStackEntry(APPS_COUNTDOWN_MANAGEMENT_ROUTE)
        }

        // APPS_COUNTDOWN_MANAGEMENT_ROUTE ViewModal is passed so that both screens can share the same
        // instance, so changes made in this screen are reflected in the management screen.
        val viewModel: CountdownSettingsViewModel = hiltViewModel(parentEntry)

        AppCountdownAdvancedScreen(
            { navController.popBackStack() },
            viewModel
        )
    }

    composable(
        APP_COUNTDOWN_APP_PICKER_ROUTE,
        enterTransition = { fadeIn(tween(SCREEN_TRANSITION_MS)) },
        exitTransition = { fadeOut(tween(SCREEN_TRANSITION_MS)) }
    ) { navBackStackEntry ->
        val parentEntry = remember(navBackStackEntry) {
            navController.getBackStackEntry(APPS_COUNTDOWN_MANAGEMENT_ROUTE)
        }

        val viewModel: CountdownSettingsViewModel = hiltViewModel(parentEntry)

        AppCountdownScreen(
            { navController.popBackStack() },
            viewModel
        )
    }
}
