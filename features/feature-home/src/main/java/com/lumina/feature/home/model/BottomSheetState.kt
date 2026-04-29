package com.lumina.feature.home.model

import androidx.compose.ui.text.font.FontFamily
import com.lumina.core.model.AppShortcut
import com.lumina.core.model.FavouriteItem
import com.lumina.core.model.LauncherItem

sealed interface BottomSheetState {
    data object None : BottomSheetState
    data class AppOptions(
        val selectedApp: SelectedApp,
        val shortcuts: List<AppShortcut> = emptyList()
    ) : BottomSheetState

    data class ShortcutOptions(
        val shortcut: LauncherItem.Shortcut,
        val isFavourite: Boolean
    ): BottomSheetState

    data object PrivateSpaceSettings : BottomSheetState
}
