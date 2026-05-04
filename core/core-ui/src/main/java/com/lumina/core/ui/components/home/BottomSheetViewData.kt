package com.lumina.core.ui.components.home

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomSheetViewData(
    val title: String,
    val icon: ImageVector? = null,
    val onHeaderClick: (() -> Unit)? = null,
    val shortcutActions: List<BottomSheetAppAction> = emptyList(),
    val quickActions: List<BottomSheetAppAction> = emptyList(),
    val actions: List<BottomSheetAppAction> = emptyList()
)
