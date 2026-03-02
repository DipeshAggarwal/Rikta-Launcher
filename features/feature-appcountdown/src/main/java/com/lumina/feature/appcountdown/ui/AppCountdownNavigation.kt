package com.lumina.feature.appcountdown.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lumina.core.ui.Motion.SCREEN_TRANSITION_DURATION
import com.lumina.feature.appcountdown.CountdownSettingsViewModel

const val COUNTDOWN_APPS_MANAGEMENT_ROUTE  = "manage_countdown_apps"
const val APP_COUNTDOWN_BULK_ROUTE = "bulk_countdown_apps"

fun NavGraphBuilder.appCountdownNavigation(
    navController: NavController
) {
    composable(
        route = COUNTDOWN_APPS_MANAGEMENT_ROUTE,
        enterTransition = { fadeIn(tween(SCREEN_TRANSITION_DURATION)) },
        exitTransition = { fadeOut(tween(SCREEN_TRANSITION_DURATION)) }
    ) { navBackStackEntry ->
        val viewModel: CountdownSettingsViewModel = hiltViewModel(navBackStackEntry)

        CountdownAppsManagementScreen(
            { navController.navigate(APP_COUNTDOWN_BULK_ROUTE) },
            { navController.popBackStack() },
            viewModel
        )
    }

    composable(
        APP_COUNTDOWN_BULK_ROUTE,
        enterTransition = { fadeIn(tween(SCREEN_TRANSITION_DURATION)) },
        exitTransition = { fadeOut(tween(SCREEN_TRANSITION_DURATION)) }
    ) { navBackStackEntry ->
        // remember is needed so that reference remains consistent across recompositions
        // Also, `getBackStackEntry` is an expensive operation and doesn't change.
        val parentEntry = remember(navBackStackEntry) {
            navController.getBackStackEntry(COUNTDOWN_APPS_MANAGEMENT_ROUTE)
        }

        // COUNTDOWN_APPS_MANAGEMENT_ROUTE ViewModal is passed so that both screens can share the same
        // instance, so changes made in this screen are reflected in the management screen.
        val viewModel: CountdownSettingsViewModel = hiltViewModel(parentEntry)

        AppCountdownScreen(
            { navController.popBackStack() },
            viewModel
        )
    }
}
