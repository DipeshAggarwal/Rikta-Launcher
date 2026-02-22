package com.lumina.feature.apphiding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.common.FlowDefaults.WhileSubscribedTimeoutMillis
import com.lumina.domain.apps.AppInfo
import com.lumina.domain.apps.HiddenAppsRepository
import com.lumina.domain.apps.InstalledAppsRepository
import com.lumina.domain.settings.SearchSettings
import com.lumina.domain.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import jakarta.inject.Inject
import kotlinx.coroutines.flow.combine

@HiltViewModel
class AppHidingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val hiddenAppsRepository: HiddenAppsRepository,
    installedAppsRepository: InstalledAppsRepository
): ViewModel() {
    // .Eagerly is used so that startup happens at creation time.
    // This improves animation and loading experience.

    // Master list of all launcher apps on the system.
    val installedApps: StateFlow<List<AppInfo>> = installedAppsRepository.installedApps()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    // Reactive set of package names currently marked as hidden.
    val hiddenPackagesSet: StateFlow<Set<String>> = hiddenAppsRepository.allHiddenApps()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptySet()
        )

    // Combined list of AppInfo specifically for currently hidden apps.
    private val groupApps: StateFlow<Pair<List<AppInfo>, List<AppInfo>>> = combine(
        installedApps,
        hiddenPackagesSet
    ) { installed, hiddenSet ->
        installed.partition { it.packageName in hiddenSet }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMillis),
        Pair(emptyList(), emptyList())
    )

    val hiddenApps: StateFlow<List<AppInfo>> = groupApps.map { it.first }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMillis),
            emptyList()
        )

    val nonHiddenApps: StateFlow<List<AppInfo>> = groupApps.map { it.second }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMillis),
            emptyList()
        )

    val searchSettings: StateFlow<SearchSettings> = settingsRepository.searchSettings
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            SearchSettings()
        )

    // Temporary
    fun launchApp(context: Context, packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent?.let { context.startActivity(it) }
    }

    fun addHiddenApp(packageName: String) {
        viewModelScope.launch {
            hiddenAppsRepository.addHiddenApp(packageName)
        }
    }

    fun removeHiddenApp(packageName: String) {
        viewModelScope.launch {
            hiddenAppsRepository.removeHiddenApp(packageName)
        }
    }

    fun toggleHidden(packageName: String) {
        viewModelScope.launch {
            val currentlySelected = hiddenPackagesSet.value.contains(packageName)

            if (currentlySelected) {
                hiddenAppsRepository.removeHiddenApp(packageName)
            } else {
                hiddenAppsRepository.addHiddenApp(packageName)
            }
        }
    }

    fun setShowHiddenAppsInSearch(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSearchSettings {
                copy(showHiddenAppsInSearch = enabled)
            }
        }
    }
}
