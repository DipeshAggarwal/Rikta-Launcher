package com.lumina.feature.appfavourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.model.AppInfo
import com.lumina.domain.apps.FavouriteAppsRepository
import com.lumina.domain.apps.InstalledAppsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import jakarta.inject.Inject

@HiltViewModel
class AppFavouriteViewModel @Inject constructor(
    private val favouriteAppsRepository: FavouriteAppsRepository,
    installedAppsRepository: InstalledAppsRepository
) : ViewModel() {
    // .Eagerly is used so that startup happens at creation time.
    // This improves animation and loading experience.
    val installedApps: StateFlow<List<AppInfo>> = installedAppsRepository.apps

    // Reactive set of package names currently marked as favourite.
    val favouritePackages: StateFlow<List<String>> = favouriteAppsRepository.appPackages
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    fun addFavouriteApp(packageName: String) {
        viewModelScope.launch {
            favouriteAppsRepository.addApp(packageName)
        }
    }

    fun removeFavouriteApp(packageName: String) {
        viewModelScope.launch {
            favouriteAppsRepository.removeApp(packageName)
        }
    }

    fun reorderFavouriteApps(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            favouriteAppsRepository.reorderApps(fromIndex, toIndex)
        }
    }
}
