package com.lumina.feature.apphiding.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lumina.core.ui.Motion.SCREEN_TRANSITION_DURATION
import com.lumina.feature.apphiding.AppHidingViewModel

const val HIDDEN_APPS_MANAGEMENT_ROUTE  = "manage_hidden_apps"
const val APP_HIDING_BULK_ROUTE = "bulk_hidden_apps"

fun NavGraphBuilder.appHidingNavigation(
    navController: NavController
) {
    composable(
        route = HIDDEN_APPS_MANAGEMENT_ROUTE,
        enterTransition = { fadeIn(tween(SCREEN_TRANSITION_DURATION)) },
        exitTransition = { fadeOut(tween(SCREEN_TRANSITION_DURATION)) }
    ) { navBackStackEntry ->
        val viewModel: AppHidingViewModel = hiltViewModel(navBackStackEntry)

        HiddenAppsManagementScreen(
            { navController.navigate(APP_HIDING_BULK_ROUTE) },
            { navController.popBackStack() },
            viewModel
        )
    }

    composable(
        APP_HIDING_BULK_ROUTE,
        enterTransition = { fadeIn(tween(SCREEN_TRANSITION_DURATION)) },
        exitTransition = { fadeOut(tween(SCREEN_TRANSITION_DURATION)) }
    ) { navBackStackEntry ->
        // remember is needed so that reference remains consistent across recompositions
        // Also, `getBackStackEntry` is an expensive operation and doesn't change.
        val parentEntry = remember(navBackStackEntry) {
            navController.getBackStackEntry(HIDDEN_APPS_MANAGEMENT_ROUTE)
        }

        // HIDDEN_APPS_MANAGEMENT_ROUTE ViewModal is passed so that both screens can share the same
        // instance, so changes made in this screen are reflected in the management screen.
        val viewModel: AppHidingViewModel = hiltViewModel(parentEntry)

        AppHidingScreen(
            { navController.popBackStack() },
            viewModel
        )
    }
}
