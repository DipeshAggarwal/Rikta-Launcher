package com.lumina.feature.profiles.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lumina.core.ui.Motion.SCREEN_TRANSITION_MS
import com.lumina.feature.profiles.ProfileDetailViewModel
import com.lumina.feature.profiles.ProfileListViewModel
import com.lumina.feature.profiles.ProfileManageViewModel
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
            onViewProfileDetails = { profileId ->
                navController.navigate(ProfileNavigationRoute.detailRoute(profileId))
            },
            onCreateNewProfile = { navController.navigate(ProfileNavigationRoute.PROFILE_CREATE_ROUTE) },
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = ProfileNavigationRoute.PROFILE_CREATE_ROUTE,
        enterTransition = { fadeIn(tween (SCREEN_TRANSITION_MS)) },
        exitTransition = { fadeOut(tween (SCREEN_TRANSITION_MS)) }
    ) { navBackStackEntry ->
        val parentEntry = remember(navBackStackEntry) {
            navController.getBackStackEntry(ProfileNavigationRoute.PROFILE_LIST_ROUTE)
        }
        val viewModel: ProfileManageViewModel = hiltViewModel(parentEntry)

        ProfileCreateScreen(
            viewModel = viewModel,
            onCustomiseSettings = {},
            onProfileCreated = {},
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = ProfileNavigationRoute.PATTERN_DETAIL_ROUTE,
        enterTransition = { fadeIn(tween (SCREEN_TRANSITION_MS)) },
        exitTransition = { fadeOut(tween (SCREEN_TRANSITION_MS)) }
    ) { navBackStackEntry ->
        val viewModel: ProfileDetailViewModel = hiltViewModel(navBackStackEntry)

        ProfileDetailScreen(
            viewModel = viewModel,
            onNavigateToSummary = {},
            onNavigateToHomeScreen = {},
            onNavigateToRestrictions = {},
            onNavigateToAllowedApps = {},
            onNavigateToTrigger = {},
            onNavigateToAppearance = {},
            onNavigateToSecurity = {},
            onProfileDuplicated = { newProfileId ->
                navController.navigate(ProfileNavigationRoute.detailRoute(newProfileId))
            },
            onProfileDeleted = { navController.popBackStack() },
            onBack = { navController.popBackStack() }
        )
    }
}
