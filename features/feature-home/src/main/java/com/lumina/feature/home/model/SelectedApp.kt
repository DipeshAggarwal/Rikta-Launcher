package com.lumina.feature.home.model

import com.lumina.core.model.AppInfo
import com.lumina.core.model.AppProfile

data class SelectedApp(
    val app: AppInfo,
    val isFavourite: Boolean,
    val isCountdownRequired: Boolean,
    val profile: AppProfile
)
