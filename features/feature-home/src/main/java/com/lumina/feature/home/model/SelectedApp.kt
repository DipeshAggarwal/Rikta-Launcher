package com.lumina.feature.home.model

import com.lumina.domain.apps.AppInfo
import com.lumina.domain.apps.AppProfile

data class SelectedApp(
    val app: AppInfo,
    val isFavourite: Boolean,
    val isCountdownRequired: Boolean,
    val profile: AppProfile
)
