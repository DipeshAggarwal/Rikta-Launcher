package com.lumina.feature.home.model

import com.lumina.core.model.AppProfile
import com.lumina.core.model.LauncherItem

data class SelectedApp(
    val app: LauncherItem.App,
    val isFavourite: Boolean,
    val isCountdownRequired: Boolean,
    val profile: AppProfile
)
