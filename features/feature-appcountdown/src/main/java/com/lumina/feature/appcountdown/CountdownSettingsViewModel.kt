package com.lumina.feature.appcountdown

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.model.AppInfo
import com.lumina.core.model.componentKey
import com.lumina.domain.coordination.usecase.ObserveActiveProfileAppsUseCase
import com.lumina.domain.coordination.usecase.ObserveActiveProfileCountdownAppsUseCase
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.settings.CountdownSettings
import com.lumina.domain.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CountdownSettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    observeActiveProfileApps: ObserveActiveProfileAppsUseCase,
    observeActiveProfileCountdownApps: ObserveActiveProfileCountdownAppsUseCase
) : ViewModel() {
    // .Eagerly is used so that startup happens at creation time.
    // This improves animation and loading experience.

    // Master list of all launcher apps on the system.
    val activeProfileApps: StateFlow<List<AppInfo>> = observeActiveProfileApps()
        .map { launcherApps -> launcherApps.map { it.info } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val countdownAppsSet: StateFlow<Set<String>> = observeActiveProfileCountdownApps()
        .map { countdownApps -> countdownApps.map { it.info.componentKey }.toSet() }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptySet()
        )

    val countdownSettings: StateFlow<CountdownSettings> = settingsRepository.countdownSettings
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            CountdownSettings()
        )

    fun addCountdownApp(app: AppInfo) {
        viewModelScope.launch {
            val activeProfile = profileRepository.activeProfile.firstOrNull() ?: return@launch
            profileRepository.updateShowCountdownForApp(
                profileId = activeProfile.id,
                packageName = app.packageName,
                userHandleNumber = app.userHandleNumber,
                show = true
            )
        }
    }

    fun removeCountdownApp(app: AppInfo) {
        viewModelScope.launch {
            val activeProfile = profileRepository.activeProfile.firstOrNull() ?: return@launch
            profileRepository.updateShowCountdownForApp(
                profileId = activeProfile.id,
                packageName = app.packageName,
                userHandleNumber = app.userHandleNumber,
                show = false
            )
        }
    }

    fun toggleCountdown(app: AppInfo) {
        viewModelScope.launch {
            val currentlySelected = countdownAppsSet.value.contains(app.packageName)

            if (currentlySelected) {
                removeCountdownApp(app)
            } else {
                addCountdownApp(app)
            }
        }
    }

    fun setAllCountdownSliders(stepDuration: Int, steps: Int? = null, wrapDuration: Long? = null) {
        val default = CountdownSettings()
        viewModelScope.launch {
            settingsRepository.updateCountdownSettings {
                copy(
                    countdownDurationPerStep = stepDuration,
                    countdownSteps = steps ?: default.countdownSteps,
                    countdownWrapDuration = wrapDuration ?: default.countdownWrapDuration
                )
            }
        }
    }

    fun setCountdownDurationPerStep(duration: Int) {
        viewModelScope.launch {
            settingsRepository.updateCountdownSettings {
                copy(countdownDurationPerStep = duration)
            }
        }
    }

    fun resetCountdownDurationPerStep() {
        val resetValue = CountdownSettings().countdownDurationPerStep
        viewModelScope.launch {
            settingsRepository.updateCountdownSettings {
                copy(countdownDurationPerStep = resetValue)
            }
        }
    }

    fun setCountdownSteps(steps: Int) {
        viewModelScope.launch {
            settingsRepository.updateCountdownSettings {
                copy(countdownSteps = steps)
            }
        }
    }

    fun resetCountdownSteps() {
        val resetValue = CountdownSettings().countdownSteps
        viewModelScope.launch {
            settingsRepository.updateCountdownSettings {
                copy(countdownSteps = resetValue)
            }
        }
    }

    fun setCountdownWrapDuration(duration: Long) {
        viewModelScope.launch {
            settingsRepository.updateCountdownSettings {
                copy(countdownWrapDuration = duration)
            }
        }
    }

    fun resetCountdownWrapDuration() {
        val resetValue = CountdownSettings().countdownWrapDuration
        viewModelScope.launch {
            settingsRepository.updateCountdownSettings {
                copy(countdownWrapDuration = resetValue)
            }
        }
    }

    fun setShowText(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateCountdownSettings {
                copy(showText = enabled)
            }
        }
    }
}