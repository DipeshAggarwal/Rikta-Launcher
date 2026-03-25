package com.lumina.domain.coordination.usecase

import com.lumina.core.model.LauncherItem
import com.lumina.core.model.SystemProfileIds
import com.lumina.domain.apps.HiddenAppsRepository
import com.lumina.domain.profiles.ProfileRepository
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull

class ObserveActiveProfileAppsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val hiddenAppsRepository: HiddenAppsRepository,
    private val appsMappingUseCase: ObserveActiveProfileAppsMappingUseCase
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<LauncherItem.App>> {
        return combine(
            profileRepository.activeProfile.filterNotNull(),
            appsMappingUseCase(),
            hiddenAppsRepository.appPackages
        ) { activeProfile, appsMap, hiddenPkg ->
            appsMap.values.filter { app ->
                if (activeProfile.id == SystemProfileIds.DEFAULT) {
                    app.info.packageName !in hiddenPkg
                } else true
            }.sortedBy { it.info.displayName.lowercase() }
        }
    }
}

