package com.lumina.feature.profiles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.common.FlowDefaults.WhileSubscribedTimeoutMs
import com.lumina.core.logging.Logger
import com.lumina.core.model.ProfileClassification
import com.lumina.core.ui.extensions.systemProfileDisplayName
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.domain.profiles.usecase.ClassifyProfileModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface ProfileDetailEvent {
    data object ProfileDeleted : ProfileDetailEvent
    data class ProfileDuplicated(val newProfileId: String) : ProfileDetailEvent
    data class ShowError(val message: String) : ProfileDetailEvent
}

data class ProfileDetailUiState(
    val isLoading: Boolean = true,
    val profile: LauncherProfile? = null,
    val isActive: Boolean = false,
    val triggerCount: Int = 0,
    val allowedAppCount: Int = 0,
    val isPerformingAction: Boolean = false,
    val classification: ProfileClassification? = null
)

@HiltViewModel
class ProfileDetailViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    classifyProfileModeUseCase: ClassifyProfileModeUseCase,
    savedStateHandle: SavedStateHandle,
    private val logger: Logger
) : ViewModel() {
    private val TAG = this::class.java.simpleName

    // Profile being viewed, provided by navigation.
    private val targetProfileId: String = checkNotNull(savedStateHandle[ProfileNavigationRoute.PROFILE_ID_ARG])
    private val _events = MutableSharedFlow<ProfileDetailEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<ProfileDetailEvent> = _events.asSharedFlow()

    private val _isPerformingAction = MutableStateFlow(false)

    val uiState: StateFlow<ProfileDetailUiState> = combine(
        profileRepository.getProfileById(targetProfileId),
        profileRepository.activeProfile,
        profileRepository.getProfileTriggers(targetProfileId),
        profileRepository.getAppsForProfile(targetProfileId),
        _isPerformingAction
    ) { profile, activeProfile, triggers, apps, isPerformingAction ->
        ProfileDetailUiState(
            isLoading = profile == null,
            profile = profile,
            isActive = activeProfile?.id == targetProfileId,
            triggerCount = triggers.size,
            allowedAppCount = apps.size,
            isPerformingAction = _isPerformingAction.value,
            classification = profile?.let { classifyProfileModeUseCase(it) }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMs),
        ProfileDetailUiState()
    )

    fun toggleActiveState() {
        viewModelScope.launch {
            if (_isPerformingAction.value) return@launch
            _isPerformingAction.value = true

            try {
                if (uiState.value.isActive) profileRepository.clearActiveProfile()
                else profileRepository.setActiveProfile(targetProfileId)
            } catch (e: Exception) {
                logger.e(TAG, "Failed to toggle active state for $targetProfileId.", e)
                _events.emit(ProfileDetailEvent.ShowError("Failed to switch profile."))
            } finally {
                _isPerformingAction.value = false
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun duplicateProfile(newName: String) {
        viewModelScope.launch {
            val currentProfile = uiState.value.profile
            if (currentProfile == null || _isPerformingAction.value) return@launch

            _isPerformingAction.value = true
            try {
                val newId = Uuid.random().toString()
                val duplicateLauncherProfile = currentProfile.copy(
                    id = newId,
                    name = "$newName (Copy)",
                    auth = currentProfile.auth.copy(activationKey = null)
                )
                profileRepository.saveProfile(duplicateLauncherProfile)
                _events.emit(ProfileDetailEvent.ProfileDuplicated(newId))
            } catch (e: Exception) {
                logger.e(TAG, "Failed to duplicate profile $targetProfileId.", e)
                _events.emit(ProfileDetailEvent.ShowError("Failed to duplicate profile."))
            } finally {
                _isPerformingAction.value = false
            }
        }
    }

    fun deleteProfile() {
        viewModelScope.launch {
            if (uiState.value.profile == null || _isPerformingAction.value) return@launch

            _isPerformingAction.value = true
            try {
                profileRepository.deleteProfile(targetProfileId)
                _events.emit(ProfileDetailEvent.ProfileDeleted)

                // Do not reset state here, otherwise it flashes with the deleted profile data.
            } catch (e: Exception) {
                logger.e(TAG, "Failed to delete profile $targetProfileId.", e)
                _events.emit(ProfileDetailEvent.ShowError("Failed to delete profile."))
                _isPerformingAction.value = false
            }
        }
    }
}
