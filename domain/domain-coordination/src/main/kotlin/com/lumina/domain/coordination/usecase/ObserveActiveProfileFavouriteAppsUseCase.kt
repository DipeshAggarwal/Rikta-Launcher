package com.lumina.domain.coordination.usecase

import com.lumina.core.model.AppInfo
import com.lumina.core.model.SystemProfileIds
import com.lumina.core.model.componentKey
import com.lumina.domain.apps.InstalledAppsRepository
import com.lumina.domain.profiles.ProfileRepository
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class ObserveActiveProfileFavouriteAppsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val installedAppsRepository: InstalledAppsRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<AppInfo>> {
        return profileRepository.activeProfile.flatMapLatest { activeProfile ->
            val profileId = activeProfile?.id ?: SystemProfileIds.DEFAULT
            combine(
                installedAppsRepository.appsMap,
                profileRepository.getFavouriteAppsList(profileId)
            ) { appsMap, profileFavs ->
                val allowedKeys: Set<String> = profileFavs
                    .map { it.appBasicData.componentKey }
                    .toSet()
                appsMap.filterKeys { it in allowedKeys }.values.toList()
            }
        }
    }
}
