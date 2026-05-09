package com.lumina.feature.profiles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.common.FlowDefaults.WhileSubscribedTimeoutMs
import com.lumina.core.logging.Logger
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.domain.profiles.model.TriggerCondition
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface ProfileDetailEvent {
    data object ProfileDeleted : ProfileDetailEvent
    data class ProfileDuplicated(val newProfileId: String) : ProfileDetailEvent
    data class ShowError(val message: String) : ProfileDetailEvent
}

@HiltViewModel
class ProfileDetailViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
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
    val isPerformingAction: StateFlow<Boolean> = _isPerformingAction.asStateFlow()

    val profile: StateFlow<LauncherProfile?> = profileRepository.getProfileById(targetProfileId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMs), null)

    val isActive: StateFlow<Boolean> = profileRepository.activeProfile
        .map { it?.id == targetProfileId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMs), false)

    val triggers: StateFlow<List<TriggerCondition>> = profileRepository.getProfileTriggers(targetProfileId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMs), emptyList())

    val allowedAppCount: StateFlow<Int> = profileRepository.getAppsForProfile(targetProfileId)
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMs), 0)

    fun toggleActiveState() {
        viewModelScope.launch {
            if (_isPerformingAction.value) return@launch
            _isPerformingAction.value = true

            try {
                if (isActive.value) profileRepository.clearActiveProfile()
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
    fun duplicateProfile() {
        viewModelScope.launch {
            val currentProfile = profile.value
            if (currentProfile == null || _isPerformingAction.value) return@launch

            _isPerformingAction.value = true
            try {
                val newId = Uuid.random().toString()
                val duplicateLauncherProfile = currentProfile.copy(
                    id = newId,
                    name = "${currentProfile.name} (Copy)",
                    settings = currentProfile.settings.copy(activationKey = null)
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
            if (profile.value == null || _isPerformingAction.value) return@launch

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
