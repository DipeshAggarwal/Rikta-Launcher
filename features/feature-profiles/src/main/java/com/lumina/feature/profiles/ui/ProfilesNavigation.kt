package com.lumina.feature.profiles.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.lumina.core.ui.Motion.SCREEN_TRANSITION_MS
import com.lumina.feature.profiles.ProfileDetailViewModel
import com.lumina.feature.profiles.ProfileListViewModel
import com.lumina.feature.profiles.ProfileManageViewModel
import com.lumina.feature.profiles.ProfileNavigationRoute
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.lumina.feature.profiles.ProfileManageEvent
import com.lumina.feature.profiles.ProfileManageUiState

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

    navigation(
        route = ProfileNavigationRoute.PROFILE_GRAPH_DETAIL_ROUTE,
        startDestination = ProfileNavigationRoute.PROFILE_DETAIL_ROUTE
    ) {
        // ProfileDetailViewModel is scoped to detail graph, so it survives user switching between the
        // edit and details screen.
        composable(
            route = ProfileNavigationRoute.PROFILE_DETAIL_ROUTE,
            enterTransition = { fadeIn(tween (SCREEN_TRANSITION_MS)) },
            exitTransition = { fadeOut(tween (SCREEN_TRANSITION_MS)) }
        ) { navBackStackEntry ->
            val detailGraphEntry = remember(navBackStackEntry) {
                navController.getBackStackEntry(ProfileNavigationRoute.PROFILE_GRAPH_DETAIL_ROUTE)
            }

            val viewModel: ProfileDetailViewModel = hiltViewModel(detailGraphEntry)

            ProfileDetailScreen(
                viewModel = viewModel,
                onNavigateToSummary = {
                    val profileId = checkNotNull(detailGraphEntry.arguments?.getString(
                        ProfileNavigationRoute.PROFILE_ID_ARG
                    ))
                    navController.navigate(ProfileNavigationRoute.editRoute(
                        profileId = profileId, route = "summary"
                    ))
                },
                onNavigateToHomeScreen = {},
                onNavigateToRules = {
                    val profileId = checkNotNull(detailGraphEntry.arguments?.getString(
                        ProfileNavigationRoute.PROFILE_ID_ARG
                    ))
                    navController.navigate(ProfileNavigationRoute.editRoute(
                        profileId = profileId, route = "rules"
                    ))
                },
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

        // This creates a sub graph that is shared across all edit screens.
        navigation(
            route = ProfileNavigationRoute.PROFILE_GRAPH_EDIT_ROUTE,
            startDestination = ProfileNavigationRoute.PROFILE_SUMMARY_ROUTE
        ) {
            composable(
                route = ProfileNavigationRoute.PROFILE_SUMMARY_ROUTE,
                enterTransition = { fadeIn(tween (SCREEN_TRANSITION_MS)) },
                exitTransition = { fadeOut(tween (SCREEN_TRANSITION_MS)) }
            ) { navBackStackEntry ->
                val editGraphEntry = remember(navBackStackEntry) {
                    navController.getBackStackEntry(ProfileNavigationRoute.PROFILE_GRAPH_EDIT_ROUTE)
                }

                val viewModel: ProfileManageViewModel = hiltViewModel(editGraphEntry)
                val uiState by viewModel.uiState.collectAsState()

                LaunchedEffect(viewModel) {
                    viewModel.events.collect { event ->
                        when (event) {
                            ProfileManageEvent.SaveSuccess -> navController.popBackStack()
                            else -> Unit
                        }
                    }
                }

                when (val state = uiState) {
                    is ProfileManageUiState.Loading -> {}
                    is ProfileManageUiState.Error -> {}
                    is ProfileManageUiState.Ready -> {
                        ProfileSummaryScreen(
                            profile = state.draftProfile,
                            classification = state.classification,
                            onNameChange = { newName -> viewModel.updateName(newName) },
                            onDescriptionChange = { newDescription ->  viewModel.updateDescription(newDescription) },
                            onBack = { viewModel.saveProfile() }
                        )
                    }
                }
            }

            composable(
                route = ProfileNavigationRoute.PROFILE_RULES_ROUTE,
                enterTransition = { fadeIn(tween (SCREEN_TRANSITION_MS)) },
                exitTransition = { fadeOut(tween (SCREEN_TRANSITION_MS)) }
            ) { navBackStackEntry ->
                val editGraphEntry = remember(navBackStackEntry) {
                    navController.getBackStackEntry(ProfileNavigationRoute.PROFILE_GRAPH_EDIT_ROUTE)
                }

                val viewModel: ProfileManageViewModel = hiltViewModel(editGraphEntry)
                val uiState by viewModel.uiState.collectAsState()

                LaunchedEffect(viewModel) {
                    viewModel.events.collect { event ->
                        when (event) {
                            ProfileManageEvent.SaveSuccess -> navController.popBackStack()
                            else -> Unit
                        }
                    }
                }

                when (val state = uiState) {
                    is ProfileManageUiState.Loading -> {}
                    is ProfileManageUiState.Error -> {}
                    is ProfileManageUiState.Ready -> {
                        ProfileRulesScreen(
                            permissions = state.draftProfile.permissions,
                            restrictions = state.draftProfile.restrictions,
                            settings = state.draftProfile.settings,
                            onPermissionsChange = { modifiedPermissions ->
                                viewModel.updatePermissions(modifiedPermissions)
                            },
                            onRestrictionsChange = { modifiedRestrictions ->
                                viewModel.updateRestrictions(modifiedRestrictions)
                            },
                            onSettingsChange = { modifiedSettings ->
                                viewModel.updateSettings(modifiedSettings)
                            },
                            onBack = { viewModel.saveProfile() }
                        )
                    }
                }
            }
        }
    }
}
