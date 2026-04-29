package com.lumina.domain.coordination.usecase

import com.lumina.core.model.FavouriteItemType
import com.lumina.core.model.LauncherItem
import com.lumina.domain.apps.InstalledAppsRepository
import com.lumina.domain.profiles.ProfileFavouriteRepository
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.shortcut.ShortcutRepository
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

class ObserveActiveProfileFavouritesUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val profileFavouriteRepository: ProfileFavouriteRepository,
    private val shortcutRepository: ShortcutRepository,
    private val appsMappingUseCase: ObserveActiveProfileAppsMappingUseCase
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<LauncherItem>> {
        return profileRepository.activeProfile
            .filterNotNull()
            .flatMapLatest { activeProfile ->
                combine(
                    appsMappingUseCase(),
                    shortcutRepository.get(activeProfile.id),
                    profileFavouriteRepository.getAll(activeProfile.id)
                ) { appsMap, shortcuts, favourites ->
                    val shortcutsMap = shortcuts.associateBy { it.id }

                    favourites.mapNotNull { favourite ->
                        when (favourite.itemType) {
                            FavouriteItemType.APP -> {
                                appsMap[favourite.itemId]?.copy(
                                    favouriteOrder = favourite.favouriteOrder
                                )
                            }
                            FavouriteItemType.SHORTCUT -> {
                                shortcutsMap[favourite.itemId]?.let { shortcut ->
                                    LauncherItem.Shortcut(
                                        id = shortcut.id,
                                        label = shortcut.label,
                                        type = shortcut.type,
                                        userHandleNumber = shortcut.userHandleNumber,
                                        shortcutPackage = shortcut.shortcutPackage,
                                        shortcutId = shortcut.shortcutId,
                                        url = shortcut.url,
                                        targetPackage = shortcut.targetPackage,
                                        favouriteOrder = favourite.favouriteOrder
                                    )
                                }

                            }
                        }
                    }.sortedBy { it.favouriteOrder }
            }
        }
    }
}
