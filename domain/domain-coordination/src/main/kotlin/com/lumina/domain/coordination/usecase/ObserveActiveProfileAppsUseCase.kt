package com.lumina.domain.coordination.usecase

import com.lumina.core.model.AppInfo
import com.lumina.core.model.SystemProfileIds
import com.lumina.core.model.componentData
import com.lumina.core.model.componentKey
import com.lumina.domain.apps.HiddenAppsRepository
import com.lumina.domain.apps.InstalledAppsRepository
import com.lumina.domain.profiles.ProfileRepository
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

// Note: Since everything is changed, profile are first clas citizen. Default profile now needs to
// return DEFAULT.
// Think about creating a Plan of Action doc.

class ObserveActiveProfileAppsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val installedAppsRepository: InstalledAppsRepository,
    private val hiddenAppsRepository: HiddenAppsRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<AppInfo>> {
        return profileRepository.activeProfile.flatMapLatest { activeProfile ->
            val profileId = activeProfile?.id ?: SystemProfileIds.DEFAULT
            if (profileId == SystemProfileIds.DEFAULT) defaultProfileApps()
            else customProfileApps(profileId)
        }
    }

    private fun defaultProfileApps(): Flow<List<AppInfo>> = combine(
        installedAppsRepository.apps,
        hiddenAppsRepository.appPackages
    ) { allApps, hiddenPackages ->
        allApps.filter { it.packageName !in hiddenPackages}
    }

    private fun customProfileApps(profileId: String): Flow<List<AppInfo>> = combine(
        installedAppsRepository.appsMap,
        profileRepository.getAppsForProfile(profileId)
    ) { appsMap, profileApps ->
        val allowedKeys: Set<String> = profileApps
            .map { it.appBasicData.componentKey }
            .toSet()
        appsMap.filterKeys { it in allowedKeys }.values.toList()
    }
}
