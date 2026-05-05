package com.lumina.feature.appfavourite.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lumina.core.ui.Motion.SCREEN_TRANSITION_MS
import com.lumina.feature.appfavourite.AppFavouriteViewModel

const val FAVOURITE_APPS_ROUTE  = "manage_favourite_apps"

fun NavGraphBuilder.appFavouriteNavigation(
    navController: NavController
) {
    composable(
        route = FAVOURITE_APPS_ROUTE,
        enterTransition = { fadeIn(tween(SCREEN_TRANSITION_MS)) },
        exitTransition = { fadeOut(tween(SCREEN_TRANSITION_MS)) }
    ) { navBackStackEntry ->
        val viewModel: AppFavouriteViewModel = hiltViewModel(navBackStackEntry)

        AppFavouriteScreen(
            { navController.popBackStack() },
            viewModel
        )
    }
}
