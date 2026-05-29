package com.lumina.core.ui

import androidx.compose.ui.unit.dp

object ThemeTokens {
    val DefaultCornerRadius = 12.dp
    val ExpandedContentMaxHeight = 320.dp
    val DefaultVerticalPadding = 2.dp
    val RowVerticalPadding = 8.dp
    val RowHorizontalPadding = 8.dp
    val DividerBottomPadding = 8.dp
    val InLineButtonSpacerWidth = 8.dp
    val CompactTextFieldHeight = 48.dp
    val CompactTextFieldContentHorizontalPadding = 16.dp

    object Icon {
        val ContainerSize = 64.dp
        val PrimarySize = 52.dp
        val LeadingSize = 40.dp
        val RowSize = 36.dp
        val InlineSize = 24.dp
        val IndicatorSize = 16.dp
    }

    object Spacing {
        val ExtraLarge = 16.dp
        val Large = 12.dp
        val Medium = 8.dp
        val Small = 4.dp
        val Tiny = 2.dp
        val None = 0.dp
    }

    object Alpha {
        val Faint = 0.08f
        val Light = 0.16f
        val Medium = 0.32f
        val Strong = 0.64f
        val Heavy = 0.80f
    }

    object Layout {
        val EmptyStateMinSize = 200.dp
    }
}
