package com.lumina.feature.profiles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.common.time.TimeProvider
import com.lumina.core.logging.Logger
import com.lumina.core.model.ProfileClassification
import com.lumina.core.model.ProfilePreset
import com.lumina.core.model.ProfileType
import com.lumina.domain.coordination.DeviceUserProvider
import com.lumina.domain.coordination.usecase.CreateDraftProfileUseCase
import com.lumina.domain.profiles.PresetDefaults
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.manager.ProfileCategorySyncManager
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.domain.profiles.model.LauncherProfileAuth
import com.lumina.domain.profiles.model.LauncherProfileOverrides
import com.lumina.domain.profiles.model.LauncherProfilePermissions
import com.lumina.domain.profiles.model.LauncherProfileRestrictions
import com.lumina.domain.profiles.model.LauncherProfileSettings
import com.lumina.domain.profiles.usecase.ClassifyProfilePresetUseCase
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

sealed interface ProfileManageEvent {
    data object SaveSuccess : ProfileManageEvent
    data object DeleteSuccess : ProfileManageEvent
}

sealed interface ProfileManageUiState {
    data object Loading : ProfileManageUiState

    data class Ready(
        val draftProfile: LauncherProfile,
        val classification: ProfileClassification,

        val isNewProfile: Boolean,
        val isSaving: Boolean = false,

        val errorMessage: String? = null
    ) : ProfileManageUiState

    // A non recoverable state while loading
    data class Error(val message: String) : ProfileManageUiState
}

@HiltViewModel
class ProfileManageViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val deviceUserProvider: DeviceUserProvider,
    private val timeProvider: TimeProvider,
    private val createDraftProfileUseCase: CreateDraftProfileUseCase,
    private val classifyProfilePresetUseCase: ClassifyProfilePresetUseCase,
    private val profileCategorySyncManager: ProfileCategorySyncManager,
    savedStateHandle: SavedStateHandle,
    private val logger: Logger
) : ViewModel() {
    private val TAG = this::class.java.simpleName

    // Profile being viewed, provided by navigation.
    private val targetProfileId: String? = savedStateHandle[ProfileNavigationRoute.PROFILE_ID_ARG]

    private val _uiState = MutableStateFlow<ProfileManageUiState>(ProfileManageUiState.Loading)
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
        val draftProfile = createDraftProfileUseCase()
        _uiState.value = ProfileManageUiState.Ready(
            draftProfile = draftProfile,
            classification = classifyProfilePresetUseCase(draftProfile),
            isNewProfile = true
        )
    }

    private fun loadExistingProfile(profileId: String) {
        viewModelScope.launch {
            val existingProfile = profileRepository.getProfileById(profileId).firstOrNull()

            if (existingProfile != null) {
                _uiState.value = ProfileManageUiState.Ready(
                    draftProfile = existingProfile,
                    classification = classifyProfilePresetUseCase(existingProfile),
                    isNewProfile = false
                )
            } else {
                logger.w(TAG, "Profile not found: $profileId.")
                _uiState.value = ProfileManageUiState.Error("Profile not found.")
            }
        }
    }

    private inline fun updateDraftProfile(transform: (LauncherProfile) -> LauncherProfile) {
        _uiState.update { state ->
            if (state !is ProfileManageUiState.Ready) return@update state

            val updatedProfile = transform(state.draftProfile)
            state.copy(
                draftProfile = updatedProfile,
                classification = classifyProfilePresetUseCase(updatedProfile),
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

    fun applyPreset(preset: ProfilePreset) {
        updateDraftProfile { PresetDefaults.forPreset(preset).applyTo(it) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state !is ProfileManageUiState.Ready) return@launch
            if (state.isSaving) return@launch

            val draftProfile = state.draftProfile
            if (draftProfile.name.isBlank()) {
                _uiState.value = state.copy(errorMessage = "Profile name cannot be empty.")
                return@launch
            }

            val key = draftProfile.auth.activationKey
            if (!key.isNullOrBlank() && !profileRepository.isActivationKeyUnique(
                    key,
                    draftProfile.id
                )
            ) {
                _uiState.value = state.copy(errorMessage = "This activation key is already in use.")
                return@launch
            }

            _uiState.value = state.copy(isSaving = true, errorMessage = null)
            try {
                val finalDraft = draftProfile.copy(updatedAt = timeProvider.now())

                if (state.isNewProfile) profileRepository.saveProfile(finalDraft)
                else profileRepository.updateProfile(finalDraft)

                profileCategorySyncManager.syncProfile(finalDraft.id)
                _events.emit(ProfileManageEvent.SaveSuccess)
            } catch (e: Exception) {
                logger.e(TAG, "Failed to save profile: ${draftProfile.id}.", e)
                _uiState.update { currentState ->
                    if (currentState !is ProfileManageUiState.Ready) currentState
                    else currentState.copy(
                        isSaving = false,
                        errorMessage = "Failed to save profile."
                    )
                }
            }
        }
    }

    fun deleteProfile() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state !is ProfileManageUiState.Ready) return@launch
            if (state.isSaving) return@launch

            val draftProfile = state.draftProfile
            _uiState.value = state.copy(isSaving = true, errorMessage = null)

            try {
                profileRepository.deleteProfile(draftProfile.id)
                _events.emit(ProfileManageEvent.DeleteSuccess)
            } catch (e: Exception) {
                logger.e(TAG, "Failed to delete profile: ${draftProfile.id}.", e)
                _uiState.update { currentState ->
                    if (currentState !is ProfileManageUiState.Ready) currentState
                    else currentState.copy(
                        isSaving = false,
                        errorMessage = "Failed to delete profile."
                    )
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { state ->
            if (state is ProfileManageUiState.Ready) state.copy(errorMessage = null)
            else state
        }
    }
}
