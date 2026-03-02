package com.lumina.feature.home.model

import com.lumina.core.model.AppShortcut

sealed interface BottomSheetState {
    data object None : BottomSheetState
    data class AppOptions(
        val selectedApp: SelectedApp,
        val shortcuts: List<AppShortcut> = emptyList()
    ) : BottomSheetState
    data object PrivateSpaceSettings : BottomSheetState
}
