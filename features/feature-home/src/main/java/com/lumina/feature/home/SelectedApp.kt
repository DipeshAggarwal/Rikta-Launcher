package com.lumina.feature.home

import com.lumina.domain.apps.AppInfo
import com.lumina.domain.apps.AppProfile

data class SelectedApp(
    val app: AppInfo,
    val profile: AppProfile
)
