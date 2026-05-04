package com.lumina.core.ui.components.home

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Action that can be shown in the bottom sheet
 * */
data class BottomSheetAppAction(
    val label: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val trailingIcon: ImageVector? = null,
)
