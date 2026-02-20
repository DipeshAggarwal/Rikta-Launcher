package com.lumina.feature.home

sealed class BottomSheetState {
    data object None : BottomSheetState()
    data class AppOptions(val selectedApp: SelectedApp) : BottomSheetState()
    data object PrivateSpaceSettings : BottomSheetState()
}
