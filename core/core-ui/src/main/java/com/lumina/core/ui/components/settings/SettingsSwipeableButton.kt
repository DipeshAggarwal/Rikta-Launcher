package com.lumina.core.ui.components.settings

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import com.lumina.core.ui.R
import com.lumina.core.ui.components.text.AutoResizingText
import com.lumina.core.ui.theme.ErrorContentColor
import com.lumina.core.ui.theme.LuminaCardDefaults

private object SettingsSwipeableButtonDefaults {
    const val SWIPE_THRESHOLD_FRACTION = 0.5f
}

/**
 * Settings button that can be swiped to be dismissed
 *
 * @param label The text to be shown
 * @param onClick When composable is clicked
 * @param onDeleteClick When composable is swiped to be deleted
 * @param isTopOfGroup Whether this item is at the top of a group of items, for corner rounding
 * @param isBottomOfGroup Whether this item is at the bottom of a group of items, for corner rounding
 */
@Composable
fun SettingsSwipeableButton(
    label: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false,
    fontFamily: FontFamily? = MaterialTheme.typography.bodyMedium.fontFamily
) {
    val currentShape = settingsGroupShape(
        isTopOfGroup = isTopOfGroup,
        isBottomOfGroup = isBottomOfGroup
    )

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * SettingsSwipeableButtonDefaults.SWIPE_THRESHOLD_FRACTION }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDeleteClick()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.padding(vertical = SettingsComponentsDefaults.VerticalPadding),
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            // Background while swiping
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = currentShape,
                colors = LuminaCardDefaults.colors()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = SettingsComponentsDefaults.HorizontalPadding),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.remove),
                        tint = ErrorContentColor
                    )
                }
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
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
                        .weight(SettingsComponentsDefaults.TEXT_WEIGHT)
                        .padding(end = SettingsComponentsDefaults.TextIconSpacing),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = fontFamily)
                )
            }
        }
    }
}
