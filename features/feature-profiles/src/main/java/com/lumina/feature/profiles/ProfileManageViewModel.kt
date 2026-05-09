package com.lumina.feature.profiles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.logging.Logger
import com.lumina.core.model.ProfileAuthMethod
import com.lumina.core.model.ProfileType
import com.lumina.domain.coordination.DeviceUserProvider
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.domain.profiles.model.LauncherProfileOverrides
import com.lumina.domain.profiles.model.LauncherProfileSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface ProfileManageEvent {
    data object SaveSuccess : ProfileManageEvent
    data object DeleteSuccess : ProfileManageEvent
}

data class ProfileManageUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isNewProfile: Boolean = true,

    val profileId: String = "",
    val name: String = "",
    val type: ProfileType = ProfileType.CUSTOM,
    val description: String? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    val settings: LauncherProfileSettings = LauncherProfileSettings(
        strictMode = false,
        isAdmin = false,
        priorityTriggerLaunch = false,
        filterNotification = false,
        allowAppRename = false,
        allowProfileManagement = false,
        allowAppCategoryChange = false,
        blockProfileTriggerSwitching = false,
        startDnd = false,
        showAppList = false,
        hideScreenTimeOnApps = false,
        disableOnLock = false,
        blockUnauthorisedApps = false,
        entryAuthMethod = ProfileAuthMethod.NONE,
        exitAuthMethod = ProfileAuthMethod.NONE,
        activationKey = null,
    ),
    val overrides: LauncherProfileOverrides = LauncherProfileOverrides(
        theme = null,
        background = null,
        font = null,
        showClock = null,
        showBigClock = null,
        showDate = null,
        showWeather = null,
        hideScreenTime = null,
        iconName = null
    ),
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileManageViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val deviceUserProvider: DeviceUserProvider,
    savedStateHandle: SavedStateHandle,
    private val logger: Logger
) : ViewModel() {
    private val TAG = this::class.java.simpleName

    // Profile being viewed, provided by navigation.
    private val targetProfileId: String? = savedStateHandle[ProfileNavigationRoute.PROFILE_ID_ARG]

    private val _uiState = MutableStateFlow(
        ProfileManageUiState(isLoading = targetProfileId != null)
    )
    val uiState: StateFlow<ProfileManageUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<ProfileManageEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<ProfileManageEvent> = _events.asSharedFlow()

    init { initialise() }

    private fun initialise() {
        if (targetProfileId == null) initialiseNewProfile()
        else loadExistingProfile(targetProfileId)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun initialiseNewProfile() {
        _uiState.update { it.copy(profileId = Uuid.random().toString()) }
    }

    private fun loadExistingProfile(profileId: String) {
        viewModelScope.launch {
            val existingProfile = profileRepository.getProfileById(profileId).firstOrNull()

            if (existingProfile != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isNewProfile = false,
                        profileId = existingProfile.id,
                        name = existingProfile.name,
                        type = existingProfile.type,
                        description = existingProfile.description,
                        createdAt = existingProfile.createdAt,
                        settings = existingProfile.settings,
                        overrides = existingProfile.overrides
                    )
                }
            } else {
                logger.w(TAG, "Profile not found: $profileId.")
                _uiState.update { it.copy(isLoading = false, errorMessage = "Profile not found.") }
            }
        }
    }

    fun updateName(newName: String) {
        _uiState.update { it.copy(name = newName, errorMessage = null) }
    }

    fun updateType(newType: ProfileType) {
        _uiState.update { it.copy(type = newType) }
    }

    fun updateSettings(modifiedSettings: (LauncherProfileSettings) -> LauncherProfileSettings) {
        _uiState.update { it.copy(settings = modifiedSettings(it.settings)) }
    }

    fun updateOverrides(modifiedOverrides: (LauncherProfileOverrides) -> LauncherProfileOverrides) {
        _uiState.update { it.copy(overrides = modifiedOverrides(it.overrides)) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            if (_uiState.value.isSaving) return@launch
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val state = _uiState.value
            if (state.name.isBlank()) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Profile name cannot be empty.") }
                return@launch
            }

            val key = state.settings.activationKey
            if (!key.isNullOrBlank() && !profileRepository.isActivationKeyUnique(key, state.profileId)) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "This activation key is already in use.") }
                return@launch
            }

            val finalState = _uiState.value
            val profile = LauncherProfile(
                id = finalState.profileId,
                userHandleNumber = deviceUserProvider.getCurrentUserSerialNumber(),
                type = finalState.type,
                name = finalState.name,
                description = finalState.description,
                createdAt = finalState.createdAt,
                updatedAt = System.currentTimeMillis(),
                settings = finalState.settings,
                overrides = finalState.overrides
            )

            try {
                if (finalState.isNewProfile) profileRepository.saveProfile(profile)
                else profileRepository.updateProfile(profile)
                _events.emit(ProfileManageEvent.SaveSuccess)
            } catch (e: Exception) {
                logger.e(TAG, "Failed to save profile: ${finalState.profileId}.", e)
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to save profile.") }
            }
        }
    }

    fun deleteProfile() {
        viewModelScope.launch {
            if (_uiState.value.isSaving) return@launch
            _uiState.update { it.copy(isSaving = true) }

            try {
                profileRepository.deleteProfile(_uiState.value.profileId)
                _events.emit(ProfileManageEvent.DeleteSuccess)
            } catch (e: Exception) {
                logger.e(TAG, "Failed to delete profile: ${_uiState.value.profileId}.", e)
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to delete profile.") }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
