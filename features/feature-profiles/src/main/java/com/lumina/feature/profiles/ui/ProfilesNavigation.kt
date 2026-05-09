package com.lumina.feature.profiles.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lumina.core.ui.Motion.SCREEN_TRANSITION_MS
import com.lumina.feature.profiles.ProfileListViewModel
import com.lumina.feature.profiles.ProfileNavigationRoute

fun NavGraphBuilder.profilesNavigation(
    navController: NavController
) {
    composable(
        route = ProfileNavigationRoute.PROFILE_LIST_ROUTE,
        enterTransition = { fadeIn(tween (SCREEN_TRANSITION_MS)) },
        exitTransition = { fadeOut(tween (SCREEN_TRANSITION_MS)) }
    ) { navBackStackEntry ->
        val viewModel: ProfileListViewModel = hiltViewModel(navBackStackEntry)

        ProfilesScreen(
            viewModel = viewModel,
            onCreateNewProfile = { navController.navigate(ProfileNavigationRoute.manageRoute()) },
            onOpenProfileOptions = { profileId ->
                navController.navigate(ProfileNavigationRoute.detailRoute(profileId))
            }
        )
    }
}
