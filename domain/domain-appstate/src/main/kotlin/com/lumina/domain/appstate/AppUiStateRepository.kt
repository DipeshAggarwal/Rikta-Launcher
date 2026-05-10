package com.lumina.domain.appstate

import kotlinx.coroutines.flow.Flow

interface AppUiStateRepository {
    val showProfilesInfoBanner: Flow<Boolean>
    suspend fun dismissProfilesInfoBanner()
}
