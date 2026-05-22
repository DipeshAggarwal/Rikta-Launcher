package com.lumina.core.ui.components.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object SettingsComponentsDefaults {
    // Content layout
    const val TEXT_WEIGHT = 1f
    const val MAX_LINES = 1

    // Layout
    val VerticalPadding = 1.dp
    val HorizontalPadding = 16.dp
    val VerticalContentPadding = 12.dp
    val ContentHeight = 48.dp
    val RowVerticalPadding = 8.dp

    // Corners
    val GroupEdgeCornerRadius = 24.dp
    val DefaultCornerRadius = 12.dp

    // Content layout
    val TextIconSpacing = 8.dp
}

data class SettingsGroupRadii(
    val topStart: Dp,
    val topEnd: Dp,
    val bottomStart: Dp,
    val bottomEnd: Dp
)

fun settingsGroupRadii(
    isTopOfGroup: Boolean,
    isBottomOfGroup: Boolean
): SettingsGroupRadii {
    val topRadius =
        if (isTopOfGroup) SettingsComponentsDefaults.GroupEdgeCornerRadius
        else 0.dp

    val bottomRadius =
        if (isBottomOfGroup) SettingsComponentsDefaults.GroupEdgeCornerRadius
        else 0.dp

    return SettingsGroupRadii(
        topStart = topRadius,
        topEnd = topRadius,
        bottomStart = bottomRadius,
        bottomEnd = bottomRadius
    )
}

fun SettingsGroupRadii.shape(): Shape =
    RoundedCornerShape(
        topStart = topStart,
        topEnd = topEnd,
        bottomStart = bottomStart,
        bottomEnd = bottomEnd
    )

@Composable
fun SettingsGroupRadii.animatedShape(): Shape {
    val topStartRadius by animateDpAsState(topStart, label = "topStart")
    val topEndRadius by animateDpAsState(topEnd, label = "topEnd")
    val bottomStartRadius by animateDpAsState(bottomStart, label = "bottomStart")
    val bottomEndRadius by animateDpAsState(bottomEnd, label = "bottomEnd")

    return RoundedCornerShape(
        topStart = topStartRadius,
        topEnd = topEndRadius,
        bottomStart = bottomStartRadius,
        bottomEnd = bottomEndRadius
    )
}

fun settingsGroupShape(isTopOfGroup: Boolean, isBottomOfGroup: Boolean): Shape =
    settingsGroupRadii(isTopOfGroup, isBottomOfGroup).shape()
