package com.lumina.core.ui.components.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumina.core.ui.R
import com.lumina.core.ui.components.settings.SettingsComponentsDefaults
import com.lumina.core.ui.theme.ContentColor

private object BottomSheetDefaults {
    const val MAX_HEIGHT_FRACTION = 0.8f

    val ContentPadding = 24.dp
    val IconSize = 45.dp
    val IconEndPadding = 10.dp
    val QuickSurfaceSize = 64.dp
    val QuickIconSize = 32.dp
    val TitleFontSize = 32.sp
    val ActionsPaddingStart = 32.dp
    val ActionsBottomPadding = 4.dp
    val DividerVerticalPadding = 15.dp
    val ActionVerticalPadding = 4.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    modifier: Modifier = Modifier,
    viewData: BottomSheetViewData,
    onDismissRequest: () -> Unit,
    sheetState: SheetState
) {
    val screenHeight = LocalWindowInfo.current.containerDpSize.height
    val scrollPos = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Box(modifier = Modifier.animateContentSize()) {
            AnimatedContent(
                targetState = viewData,
                label = "bottom_sheet_menu_transition"
            ) { currentData ->
                val scrollPos = rememberScrollState()

                Column(
                    modifier
                        .heightIn(max = screenHeight * BottomSheetDefaults.MAX_HEIGHT_FRACTION)
                        .fillMaxWidth()
                        .padding(
                            start = BottomSheetDefaults.ContentPadding,
                            end = BottomSheetDefaults.ContentPadding
                        )
                        .verticalScroll(scrollPos)
                ) {
                    // Header
                    Row (
                        modifier = if (currentData.onHeaderClick != null)
                            Modifier.clickable(onClick = currentData.onHeaderClick)
                        else
                            Modifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentData.icon == null) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.app_options),
                                tint = ContentColor,
                                modifier = Modifier
                                    .size(BottomSheetDefaults.IconSize)
                                    .padding(end = BottomSheetDefaults.IconEndPadding)
                            )
                        } else {
                            Icon(
                                imageVector = currentData.icon,
                                contentDescription = stringResource(R.string.app_options),
                                tint = ContentColor,
                                modifier = Modifier
                                    .size(BottomSheetDefaults.IconSize)
                                    .padding(end = BottomSheetDefaults.IconEndPadding)
                            )
                        }
                        Text(
                            currentData.title,
                            color = ContentColor,
                            fontSize = BottomSheetDefaults.TitleFontSize,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = BottomSheetDefaults.DividerVerticalPadding))

                    if (currentData.quickActions.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = BottomSheetDefaults.ActionsPaddingStart
                                ),
                            horizontalArrangement = Arrangement.spacedBy(SettingsComponentsDefaults.TextIconSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            currentData.quickActions.forEach { action ->
                                key(action.label) {
                                    Surface(
                                        onClick = { action.onClick() },
                                        modifier = Modifier
                                            .weight(SettingsComponentsDefaults.TEXT_WEIGHT)
                                            .size(BottomSheetDefaults.QuickSurfaceSize),
                                        shape = RoundedCornerShape(SettingsComponentsDefaults.DefaultCornerRadius),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (action.icon != null) {
                                                Icon(
                                                    imageVector = action.icon,
                                                    contentDescription = action.label,
                                                    modifier = Modifier.size(BottomSheetDefaults.QuickIconSize)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        HorizontalDivider(Modifier.padding(
                            vertical = BottomSheetDefaults.DividerVerticalPadding))
                    }

                    // Actions
                    Column(Modifier.padding(
                        start = BottomSheetDefaults.ActionsPaddingStart,
                        bottom = BottomSheetDefaults.ActionsBottomPadding
                    )) {
                        if (currentData.shortcutActions.isNotEmpty()) {
                            currentData.shortcutActions.forEach { action ->
                                key(action.label) {
                                    Text(
                                        text = action.label,
                                        modifier = Modifier
                                            .padding(vertical = BottomSheetDefaults.ActionVerticalPadding)
                                            .combinedClickable(onClick = action.onClick),
                                        color = ContentColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            HorizontalDivider(Modifier.padding(vertical = BottomSheetDefaults.DividerVerticalPadding))
                        }

                        currentData.actions.forEach { action ->
                            key(action.label) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(onClick = action.onClick )
                                        .padding(vertical = BottomSheetDefaults.ActionVerticalPadding),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (action.icon != null) {
                                        Icon(
                                            imageVector = action.icon,
                                            contentDescription = null,
                                            tint = ContentColor
                                        )
                                    }
                                    Text(
                                        text = action.label,
                                        modifier = Modifier
                                            .padding(vertical = BottomSheetDefaults.ActionVerticalPadding),
                                        color = ContentColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (action.trailingIcon != null) {
                                        Icon(
                                            imageVector = action.trailingIcon,
                                            contentDescription = null,
                                            tint = ContentColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
