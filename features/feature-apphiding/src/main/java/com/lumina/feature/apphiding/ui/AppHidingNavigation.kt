package com.lumina.feature.apphiding.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lumina.core.ui.Motion.SCREEN_TRANSITION_DURATION

const val HIDDEN_APPS_MANAGEMENT_ROUTE  = "manage_hidden_apps"
const val APP_HIDING_BULK_ROUTE = "bulk_hidden_apps"

fun NavGraphBuilder.appHidingNavigation(
    launcherPackageName: String,
    navController: NavController
) {
    composable(
        HIDDEN_APPS_MANAGEMENT_ROUTE,
        enterTransition = { fadeIn(tween(SCREEN_TRANSITION_DURATION)) },
        exitTransition = { fadeOut(tween(SCREEN_TRANSITION_DURATION)) }
    ) { navBackStackEntry ->
        HiddenAppsManagementScreen(
            { navController.navigate(APP_HIDING_BULK_ROUTE) },
            { navController.popBackStack() }
        )
    }

    composable(
        APP_HIDING_BULK_ROUTE,
        enterTransition = { fadeIn(tween(SCREEN_TRANSITION_DURATION)) },
        exitTransition = { fadeOut(tween(SCREEN_TRANSITION_DURATION)) }
    ) {
        AppHidingScreen(launcherPackageName, { navController.popBackStack() })
    }
}
