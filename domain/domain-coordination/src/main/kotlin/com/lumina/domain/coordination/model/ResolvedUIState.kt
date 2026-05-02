package com.lumina.domain.coordination.model

import com.lumina.domain.settings.AppListSettings
import com.lumina.domain.settings.CountdownSettings
import com.lumina.domain.settings.HomeSettings
import com.lumina.domain.settings.LayoutSettings
import com.lumina.domain.settings.SearchSettings

data class ResolvedUIState(
    val home: HomeSettings,
    val appList: AppListSettings,
    val search: SearchSettings,
    val countdown: CountdownSettings,
    val layout: LayoutSettings,
    val theme: ResolvedThemeState
)

data class ResolvedThemeState(
    val background: String?,
    val font: String?
)
