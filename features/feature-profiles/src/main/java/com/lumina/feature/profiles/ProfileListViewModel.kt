package com.lumina.feature.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.common.FlowDefaults.WhileSubscribedTimeoutMs
import com.lumina.core.logging.Logger
import com.lumina.domain.appstate.AppUiStateRepository
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.domain.profiles.model.ProfileSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileListViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val appUiStateRepository: AppUiStateRepository,
    private val logger: Logger
) : ViewModel() {
    private val TAG = this::class.java.simpleName

    val activeProfileId: StateFlow<String?> = profileRepository.activeProfile
        .map { it?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMs), null)

    val profileSummaries: StateFlow<List<ProfileSummary>> = profileRepository.getAllProfileSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMs), emptyList())

    val showingProfileInfoBanner: StateFlow<Boolean> = appUiStateRepository.showProfilesInfoBanner
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMs),true)

    fun switchProfile(profileId: String) {
        viewModelScope.launch {
            try {
                if (activeProfileId.value == profileId) profileRepository.clearActiveProfile()
                else  profileRepository.setActiveProfile(profileId)
            } catch (e: Exception) {
                logger.e(TAG, "Failed to switch profile $profileId", e)
            }
        }
    }

    fun onDismissProfileInfo() {
        viewModelScope.launch {
            appUiStateRepository.dismissProfilesInfoBanner()
        }
    }
}
