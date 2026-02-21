package com.lumina.core.ui.components.home

/**
 * Action that can be shown in the bottom sheet
 * */
data class BottomSheetAppAction(
    val label: String,
    val onClick: () -> Unit
)
