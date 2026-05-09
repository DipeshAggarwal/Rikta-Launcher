package com.lumina.feature.profiles.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumina.feature.profiles.ProfileListViewModel

@Composable
fun ProfilesScreen(
    viewModel: ProfileListViewModel = hiltViewModel(),
    onCreateNewProfile: () -> Unit,
    onOpenProfileOptions: (String) -> Unit
) {
    val activeProfileId by viewModel.activeProfileId.collectAsStateWithLifecycle()
    val summaries by viewModel.profileSummaries.collectAsStateWithLifecycle()

    val activeProfileSummary = summaries.find { it.profile.id == activeProfileId }
    val inactiveProfileSummaries = summaries.filter { it.profile.id != activeProfileId }

    ProfileListScreen(
        activeProfileSummary = activeProfileSummary,
        inactiveProfileSummaries = inactiveProfileSummaries,
        onCreateNewProfile = onCreateNewProfile,
        onSwitchToProfile = { profileId -> viewModel.switchProfile(profileId) },
        onOpenProfileOptions = onOpenProfileOptions
    )
}
