package com.lumina.feature.apphiding

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.common.FlowDefaults.WhileSubscribedTimeoutMillis
import com.lumina.domain.apps.AppInfo
import com.lumina.domain.apps.HiddenAppsRepository
import com.lumina.domain.apps.InstalledAppsRepository
import com.lumina.domain.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppHidingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val hiddenAppsRepository: HiddenAppsRepository,
    private val installedAppsRepository: InstalledAppsRepository
): ViewModel() {
    val installedApps: StateFlow<List<AppInfo>> = flow {
        emit(installedAppsRepository.getInstalledApps())
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMillis),
        emptyList()
    )
    val hiddenApps: StateFlow<List<AppInfo>> = hiddenAppsRepository.allHiddenApps()
        .map { packageNames ->
            packageNames.mapNotNull { pkg ->
                try {
                    val name = installedAppsRepository.getDisplayName(pkg)
                    AppInfo(pkg, name)
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMillis),
            emptyList()
        )

    val showHiddenAppsInSearch: StateFlow<Boolean> = settingsRepository.showHiddenAppsInSearch()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMillis),
            false
        )

    val hiddenPackagesSet: StateFlow<Set<String>> = hiddenAppsRepository.allHiddenApps()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMillis),
            emptySet()
        )
    
    init {
        viewModelScope.launch {
            combine(
                hiddenAppsRepository.allHiddenApps(),
                installedApps
            ) { hiddenPackages, installedPackages ->
                val installedPackagesSet = installedPackages.map { it.packageName }.toSet()
                val cleanedHiddenPackages = hiddenPackages.filter { it in installedPackagesSet }

                Pair(hiddenPackages, cleanedHiddenPackages)
            }
                .distinctUntilChanged()
                .collect { (hiddenPackages, cleanedHiddenPackages) ->
                    if (hiddenPackages == cleanedHiddenPackages) return@collect
                    hiddenAppsRepository.setHiddenApps(cleanedHiddenPackages)
                }
        }
    }

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
            settingsRepository.setShowHiddenAppsInSearch(enabled)
        }
    }
}
