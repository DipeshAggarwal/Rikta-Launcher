package com.lumina.feature.appfavourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.model.AppInfo
import com.lumina.core.model.FavouriteItemType
import com.lumina.core.model.LauncherItem
import com.lumina.core.model.componentKey
import com.lumina.domain.coordination.usecase.ObserveActiveProfileAppsUseCase
import com.lumina.domain.coordination.usecase.ObserveActiveProfileFavouritesUseCase
import com.lumina.domain.profiles.ProfileFavouriteRepository
import com.lumina.domain.profiles.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import jakarta.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

@HiltViewModel
class AppFavouriteViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val profileFavouriteRepository: ProfileFavouriteRepository,
    observeActiveProfileApps: ObserveActiveProfileAppsUseCase,
    observeActiveProfileFavourites: ObserveActiveProfileFavouritesUseCase
) : ViewModel() {
    // .Eagerly is used so that startup happens at creation time.
    // This improves animation and loading experience.
    val activeProfileApps: StateFlow<List<AppInfo>> = observeActiveProfileApps()
        .map { launcherApps -> launcherApps.map { it.info } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Reactive set of package names currently marked as favourite.
    val favouriteItems: StateFlow<List<LauncherItem>> = observeActiveProfileFavourites()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val favouritePackages: StateFlow<List<String>> = favouriteItems
        .map { favItems ->
            favItems.map { item ->
                when (item) {
                    is LauncherItem.App -> item.info.componentKey
                    is LauncherItem.Shortcut -> item.id
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addFavourite(item: LauncherItem) {
        viewModelScope.launch {
            val activeProfile = profileRepository.activeProfile.firstOrNull() ?: return@launch
            val (itemId, itemType) = when (item) {
                is LauncherItem.App -> item.info.packageName to FavouriteItemType.APP
                is LauncherItem.Shortcut -> item.id to FavouriteItemType.SHORTCUT
            }
            profileFavouriteRepository.toggle(
                profileId = activeProfile.id,
                itemId = itemId,
                itemType = itemType
            )
        }
    }

    fun removeFavourite(item: LauncherItem) {
        viewModelScope.launch {
            val activeProfile = profileRepository.activeProfile.firstOrNull() ?: return@launch
            val (itemId, itemType) = when (item) {
                is LauncherItem.App -> item.info.packageName to FavouriteItemType.APP
                is LauncherItem.Shortcut -> item.id to FavouriteItemType.SHORTCUT
            }
            profileFavouriteRepository.toggle(
                profileId = activeProfile.id,
                itemId = itemId,
                itemType = itemType
            )
        }
    }

    fun reorderFavouriteApps(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val activeProfile = profileRepository.activeProfile.firstOrNull() ?: return@launch
            val currentFavourites = favouriteItems.value

            if (fromIndex in currentFavourites.indices && toIndex in currentFavourites.indices) {
                val item = currentFavourites[fromIndex]
                val (itemId, itemType) = when (item) {
                    is LauncherItem.App -> item.info.packageName to FavouriteItemType.APP
                    is LauncherItem.Shortcut -> item.id to FavouriteItemType.SHORTCUT
                }

                profileFavouriteRepository.update(
                    profileId = activeProfile.id,
                    itemId = itemId,
                    previous = if (toIndex > 0) toIndex - 1 else null,
                    next = if (toIndex < currentFavourites.size - 1) toIndex + 1 else null
                )
            }
        }
    }
}
