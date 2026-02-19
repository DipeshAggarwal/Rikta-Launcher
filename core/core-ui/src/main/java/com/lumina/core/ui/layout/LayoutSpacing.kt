package com.lumina.core.ui.layout

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lumina.core.common.AppDefaults.DEFAULT_SPACER_HEIGHT

data class LayoutSpacing (
    val spacerHeight: Dp
)

private val DefaultLayoutSpacing = LayoutSpacing(spacerHeight = DEFAULT_SPACER_HEIGHT.dp)

val LocalLayoutSpacing = staticCompositionLocalOf {
    DefaultLayoutSpacing
}
