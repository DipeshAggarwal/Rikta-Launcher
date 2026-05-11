package com.lumina.feature.profiles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.common.FlowDefaults.WhileSubscribedTimeoutMs
import com.lumina.core.common.time.TimeProvider
import com.lumina.core.logging.Logger
import com.lumina.core.model.ProfileClassification
import com.lumina.core.model.ProfileType
import com.lumina.domain.coordination.DeviceUserProvider
import com.lumina.domain.coordination.usecase.CreateDraftProfileUseCase
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.domain.profiles.model.LauncherProfileAuth
import com.lumina.domain.profiles.model.LauncherProfileOverrides
import com.lumina.domain.profiles.model.LauncherProfilePermissions
import com.lumina.domain.profiles.model.LauncherProfileRestrictions
import com.lumina.domain.profiles.model.LauncherProfileSettings
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

sealed interface ProfileManageEvent {
    data object SaveSuccess : ProfileManageEvent
    data object DeleteSuccess : ProfileManageEvent
}

data class ProfileManageUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isNewProfile: Boolean = true,

    val draftProfile: LauncherProfile? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileManageViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val deviceUserProvider: DeviceUserProvider,
    private val timeProvider: TimeProvider,
    private val createDraftProfileUseCase: CreateDraftProfileUseCase,
    private val classifyProfileModeUseCase: ClassifyProfileModeUseCase,
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

    val classification: StateFlow<ProfileClassification?> = uiState
        .mapNotNull { it.draftProfile }
        .map(classifyProfileModeUseCase::invoke)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMs), null)

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
        val draftProfile = createDraftProfileUseCase()
        _uiState.update { it.copy(draftProfile = draftProfile) }
    }

    private fun loadExistingProfile(profileId: String) {
        viewModelScope.launch {
            val existingProfile = profileRepository.getProfileById(profileId).firstOrNull()

            if (existingProfile != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isNewProfile = false,
                        draftProfile = existingProfile
                    )
                }
            } else {
                logger.w(TAG, "Profile not found: $profileId.")
                _uiState.update { it.copy(isLoading = false, errorMessage = "Profile not found.") }
            }
        }
    }

    private inline fun updateDraftProfile(transform: (LauncherProfile) -> LauncherProfile) {
        _uiState.update { state ->
            val draftProfile = state.draftProfile ?: return@update state

            state.copy(
                draftProfile = transform(draftProfile),
                errorMessage = null
            )
        }
    }

    fun updateName(newName: String) {
        updateDraftProfile { it.copy(name = newName) }
    }

    fun updateDescription(newDescription: String) {
        updateDraftProfile { it.copy(description = newDescription.ifBlank { null }) }
    }

    fun updateType(newType: ProfileType) {
        updateDraftProfile { it.copy(type = newType) }
    }

    fun updateSettings(modifiedSettings: (LauncherProfileSettings) -> LauncherProfileSettings) {
        updateDraftProfile { it.copy(settings = modifiedSettings(it.settings)) }
    }

    fun updatePermissions(modifiedPermissions: (LauncherProfilePermissions) -> LauncherProfilePermissions) {
        updateDraftProfile { it.copy(permissions = modifiedPermissions(it.permissions)) }
    }

    fun updateRestrictions(modifiedRestrictions: (LauncherProfileRestrictions) -> LauncherProfileRestrictions) {
        updateDraftProfile { it.copy(restrictions = modifiedRestrictions(it.restrictions)) }
    }

    fun updateAuth(modifiedAuth: (LauncherProfileAuth) -> LauncherProfileAuth) {
        updateDraftProfile { it.copy(auth = modifiedAuth(it.auth)) }
    }

    fun updateOverrides(modifiedOverrides: (LauncherProfileOverrides) -> LauncherProfileOverrides) {
        updateDraftProfile { it.copy(overrides = modifiedOverrides(it.overrides)) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            if (_uiState.value.isSaving) return@launch

            val state = _uiState.value
            val draftProfile = state.draftProfile ?: return@launch

            if (draftProfile.name.isBlank()) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Profile name cannot be empty.") }
                return@launch
            }

            val key = draftProfile.auth.activationKey
            if (!key.isNullOrBlank() && !profileRepository.isActivationKeyUnique(key, draftProfile.id)) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = "This activation key is already in use.")
                }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                val finalDraft = draftProfile.copy(updatedAt = timeProvider.now())

                if (state.isNewProfile) profileRepository.saveProfile(finalDraft)
                else profileRepository.updateProfile(finalDraft)

                _events.emit(ProfileManageEvent.SaveSuccess)
            } catch (e: Exception) {
                logger.e(TAG, "Failed to save profile: ${draftProfile.id}.", e)
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to save profile.") }
            }
        }
    }

    fun deleteProfile() {
        viewModelScope.launch {
            if (_uiState.value.isSaving) return@launch

            val draftProfile = _uiState.value.draftProfile ?: return@launch
            _uiState.update { it.copy(isSaving = true) }

            try {
                profileRepository.deleteProfile(draftProfile.id)
                _events.emit(ProfileManageEvent.DeleteSuccess)
            } catch (e: Exception) {
                logger.e(TAG, "Failed to delete profile: ${draftProfile.id}.", e)
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to delete profile.") }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
