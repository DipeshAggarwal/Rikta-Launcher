package com.lumina.domain.coordination.usecase

import com.lumina.core.model.LauncherItem
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveActiveProfileCountdownAppsUseCase @Inject constructor(
    private val appsMappingUseCase: ObserveActiveProfileAppsMappingUseCase
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<LauncherItem.App>> {
        return appsMappingUseCase().map { appMap ->
            appMap.values.filter { it.showCountdown }
        }
    }
}
