package com.lumina.core.ui.components.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.Motion
import com.lumina.core.ui.components.text.AutoResizingText
import com.lumina.core.ui.theme.CardContainerColor
import com.lumina.core.ui.theme.CardContainerColorDisabled
import com.lumina.core.ui.theme.ContentColor
import com.lumina.core.ui.theme.ContentColorDisabled
import com.lumina.core.ui.theme.LuminaCardDefaults

private object SettingsButtonDefaults {
    const val SELECTED_ALPHA = 0.5f
}

private object SettingsNavigationItemDefaults {
    const val ICON_ROTATION_DIAGONAL = -45f
    val IconSize = 24.dp
}

private object SettingsNavigationRowDefaults {
    val TextEndPadding = 8.dp
    val TextAlpha = 0.64f
    val IconSize = 36.dp
}

/**
 * Settings navigation item with label and arrow
 *
 * @param label The text to be shown
 * @param onClick When composable is clicked
 * @param isTopOfGroup Whether this item is at the top of a group of items, for corner rounding
 * @param isBottomOfGroup Whether this item is at the bottom of a group of items, for corner rounding
 * @param useAutoResize Whether to use the expensive AutoResizingText or a standard Text with ellipsis
 */
@Composable
fun SettingsButton(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false,
    fontFamily: FontFamily? = MaterialTheme.typography.bodyMedium.fontFamily,
    isDisabled: Boolean = false,
    isSelected: Boolean = false,
    useAutoResize: Boolean = true
) {
    val currentShape = settingsGroupRadii(isTopOfGroup, isBottomOfGroup).animatedShape()

    val animatedContainerColor by animateColorAsState(
        targetValue = when {
            isDisabled -> CardContainerColorDisabled
            isSelected -> CardContainerColor.copy(alpha = SettingsButtonDefaults.SELECTED_ALPHA)
            else -> CardContainerColor
        },
        animationSpec = tween(durationMillis = Motion.SCREEN_TRANSITION_DURATION),
        label = "containerColor"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (!isDisabled) ContentColor else ContentColorDisabled,
        animationSpec = tween(durationMillis = Motion.SCREEN_TRANSITION_DURATION),
        label = "contentColor"
    )

    Card(
        modifier = modifier
            .padding(vertical = SettingsComponentsDefaults.VerticalPadding)
            .clip(currentShape)
            .combinedClickable(onClick = onClick),
        shape = currentShape,
        colors = CardDefaults.cardColors(
            containerColor = animatedContainerColor,
            contentColor = animatedContentColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SettingsComponentsDefaults.HorizontalPadding,
                    vertical = SettingsComponentsDefaults.VerticalContentPadding
                )
                .height(SettingsComponentsDefaults.ContentHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (useAutoResize) {
                AutoResizingText(
                    text = label,
                    modifier = Modifier
                        .weight(SettingsComponentsDefaults.TEXT_WEIGHT)
                        .padding(end = SettingsComponentsDefaults.TextIconSpacing),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = fontFamily
                )
            } else {
                Text(
                    text = label,
                    modifier = Modifier
                        .weight(SettingsComponentsDefaults.TEXT_WEIGHT)
                        .padding(end = SettingsComponentsDefaults.TextIconSpacing),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = fontFamily,
                    maxLines = SettingsComponentsDefaults.MAX_LINES,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Settings navigation item with label and arrow
 *
 * @param label The text to be shown
 * @param diagonalArrow Whether the arrow should be pointed upwards to signal that pressing this will take you out of Escape Launcher
 * @param onClick When composable is clicked
 * @param isTopOfGroup Whether this item is at the top of a group of items, for corner rounding
 * @param isBottomOfGroup Whether this item is at the bottom of a group of items, for corner rounding
 */
@Composable
fun SettingsNavigationItem(
    label: String,
    diagonalArrow: Boolean?,
    onClick: () -> Unit,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false
) {
    val currentShape = settingsGroupShape(
        isTopOfGroup = isTopOfGroup,
        isBottomOfGroup = isBottomOfGroup
    )

    Card(
        modifier = Modifier
            .padding(vertical = SettingsComponentsDefaults.VerticalPadding)
            .clip(currentShape)
            .combinedClickable(onClick = onClick),
        shape = currentShape,
        colors = LuminaCardDefaults.colors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SettingsComponentsDefaults.HorizontalPadding,
                    vertical = SettingsComponentsDefaults.VerticalContentPadding
                )
                .height(SettingsComponentsDefaults.ContentHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AutoResizingText(
                text = label,
                modifier = Modifier
                    .weight(SettingsComponentsDefaults.TEXT_WEIGHT) // Allow text to take available space
                    .padding(end = SettingsComponentsDefaults.TextIconSpacing), // Add space between text and icon
                style = MaterialTheme.typography.bodyMedium,
            )
            val iconModifier = Modifier.size(SettingsNavigationItemDefaults.IconSize) // Standardized icon size slightly
            if (diagonalArrow == true) { // Explicitly check for true
                Icon(
                    Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = null, // Content description can be null for decorative icons
                    modifier = iconModifier.rotate(SettingsNavigationItemDefaults.ICON_ROTATION_DIAGONAL),
                    tint = ContentColor,
                )
            } else {
                Icon(
                    Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = null, // Content description can be null for decorative icons
                    modifier = iconModifier,
                    tint = ContentColor,
                )
            }
        }
    }
}

@Composable
fun SettingsNavigationRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = SettingsComponentsDefaults.HorizontalPadding,
                vertical = SettingsComponentsDefaults.RowVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = ContentColor,
            maxLines = SettingsComponentsDefaults.MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(SettingsComponentsDefaults.TEXT_WEIGHT)
                .padding(SettingsNavigationRowDefaults.TextEndPadding)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = SettingsNavigationRowDefaults.TextAlpha),
            modifier = Modifier.size(SettingsNavigationRowDefaults.IconSize)
        )
    }
}
