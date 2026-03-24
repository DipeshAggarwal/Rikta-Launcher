package com.lumina.domain.coordination.usecase

import com.lumina.core.model.AppBasicData
import com.lumina.core.model.AppOverrideState
import com.lumina.core.model.SystemProfileIds
import com.lumina.core.model.componentKey
import com.lumina.domain.apps.InstalledAppsRepository
import com.lumina.domain.profiles.ProfileRepository
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class ObserveActiveProfileCountdownAppsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val installedAppsRepository: InstalledAppsRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<AppOverrideState>> {
        return profileRepository.activeProfile.flatMapLatest { activeProfile ->
            val profileId = activeProfile?.id ?: SystemProfileIds.DEFAULT
            combine(
                installedAppsRepository.appsMap,
                profileRepository.getAppsForProfile(profileId)
            ) { appsMap, profileApps ->
                profileApps.filter { it.showCountdown }
                    .mapNotNull { appOverride ->
                        appsMap[appOverride.appBasicData.componentKey]?.let { info ->
                            AppOverrideState(
                                AppBasicData(info.packageName, info.userHandleNumber),
                                appOverride.favouriteOrder,
                                true,
                                appOverride.recommendedUsageMinutes
                            )
                        }
                    }
            }
        }
    }
}
