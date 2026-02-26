package com.lumina.feature.home.model

sealed interface BottomSheetState {
    data object None : BottomSheetState
    data class AppOptions(val selectedApp: SelectedApp) : BottomSheetState
    data object PrivateSpaceSettings : BottomSheetState
}
