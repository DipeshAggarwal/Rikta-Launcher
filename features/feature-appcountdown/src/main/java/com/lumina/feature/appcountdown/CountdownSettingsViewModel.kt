package com.lumina.feature.appcountdown

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.common.FlowDefaults.WhileSubscribedTimeoutMillis
import com.lumina.core.model.AppInfo
import com.lumina.domain.apps.InstalledAppsRepository
import com.lumina.domain.countdown.CountdownAppsRepository
import com.lumina.domain.settings.CountdownSettings
import com.lumina.domain.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CountdownSettingsViewModel @Inject constructor(
    private val countdownRepository: CountdownAppsRepository,
    private val settingsRepository: SettingsRepository,
    installedAppsRepository: InstalledAppsRepository
) : ViewModel() {
    // .Eagerly is used so that startup happens at creation time.
    // This improves animation and loading experience.

    // Master list of all launcher apps on the system.
    val installedApps: StateFlow<List<AppInfo>> = installedAppsRepository.apps
    val countdownAppsSet: StateFlow<Set<String>> = countdownRepository.appPackages
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptySet()
        )

    val countdownApps: StateFlow<List<AppInfo>> = combine(
        installedApps,
        countdownAppsSet
    ) { apps, countdownPackages ->
        apps.filter { it.packageName in countdownPackages }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMillis),
        emptyList()
    )

    val countdownSettings: StateFlow<CountdownSettings> = settingsRepository.countdownSettings
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            CountdownSettings()
        )

    fun addCountdownApp(packageName: String) {
        viewModelScope.launch {
            countdownRepository.addApp(packageName)
        }
    }

    fun removeCountdownApp(packageName: String) {
        viewModelScope.launch {
            countdownRepository.removeApp(packageName)
        }
    }

    fun toggleCountdown(packageName: String) {
        viewModelScope.launch {
            val currentlySelected = countdownAppsSet.value.contains(packageName)

            if (currentlySelected) {
                countdownRepository.removeApp(packageName)
            } else {
                countdownRepository.addApp(packageName)
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

    fun resetAndGetCountdownDurationPerStep(): Int {
        val resetValue = CountdownSettings().countdownDurationPerStep
        viewModelScope.launch {
            settingsRepository.updateCountdownSettings {
                copy(countdownDurationPerStep = resetValue)
            }
        }

        return resetValue
    }

    fun setCountdownSteps(steps: Int) {
        viewModelScope.launch {
            settingsRepository.updateCountdownSettings {
                copy(countdownSteps = steps)
            }
        }
    }

    fun resetAndGetCountdownSteps(): Int {
        val resetValue = CountdownSettings().countdownSteps
        viewModelScope.launch {
            settingsRepository.updateCountdownSettings {
                copy(countdownSteps = resetValue)
            }
        }

        return resetValue
    }

    fun setCountdownWrapDuration(duration: Long) {
        viewModelScope.launch {
            settingsRepository.updateCountdownSettings {
                copy(countdownWrapDuration = duration)
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