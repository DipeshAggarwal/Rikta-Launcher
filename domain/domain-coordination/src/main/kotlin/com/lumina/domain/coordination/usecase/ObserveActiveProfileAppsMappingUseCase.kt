package com.lumina.domain.coordination.usecase

import com.lumina.core.model.LauncherItem
import com.lumina.core.model.componentKey
import com.lumina.domain.apps.InstalledAppsRepository
import com.lumina.domain.profiles.ProfileRepository
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

class ObserveActiveProfileAppsMappingUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val installedAppsRepository: InstalledAppsRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Map<String, LauncherItem.App>> {
        return profileRepository.activeProfile
            .filterNotNull()
            .flatMapLatest { activeProfile ->
                combine(
                    installedAppsRepository.appsMap,
                    profileRepository.getAppsForProfile(activeProfile.id)
                ) { appsMap, profileApps ->
                    val overrideMap = profileApps.associateBy { it.appBasicData.componentKey }

                    appsMap.mapValues { (key, info) ->
                        val appOverride = overrideMap[key]
                        LauncherItem.App(
                            info = info,
                            showCountdown = appOverride?.showCountdown ?: false,
                            recommendedUsageMinutes = appOverride?.recommendedUsageMinutes,
                            favouriteOrder = null
                        )
                    }
                }
        }
    }
}
