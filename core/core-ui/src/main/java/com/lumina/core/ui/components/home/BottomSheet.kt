package com.lumina.core.ui.components.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumina.core.ui.R
import com.lumina.core.ui.ThemeDimensions
import com.lumina.core.ui.components.settings.SettingsComponentsDefaults
import com.lumina.core.ui.theme.ContentColor

private object BottomSheetDefaults {
    const val MAX_HEIGHT_FRACTION = 0.8f
    const val TRANSITION_DURATION = 200

    val ContentPadding = 24.dp
    val IconSize = 42.dp
    val IconEndPadding = 10.dp
    val QuickSurfaceSize = 64.dp
    val QuickIconSize = 32.dp
    val TitleFontSize = 28.sp
    val ActionsPaddingStart = 32.dp
    val ActionsBottomPadding = 4.dp
    val DividerVerticalPadding = 15.dp
    val ActionVerticalPadding = 4.dp
}

private object ActionItemDefaults {
    val DimmedAlpha = 0.3f
    val DefaultAlpha = 1.0f
    val DimmedBlur = 4.dp
    val DefaultBlur = 0.dp
    val VerticalPadding = 16.dp
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

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        AnimatedContent(
            targetState = viewData,
            contentKey = { it.title },
            label = "bottom_sheet_menu_transition",
            transitionSpec = {
                (fadeIn(tween(BottomSheetDefaults.TRANSITION_DURATION)) togetherWith
                    fadeOut(tween(BottomSheetDefaults.TRANSITION_DURATION)))
                    .using(
                        SizeTransform(clip = false) { _, _ ->
                            tween(BottomSheetDefaults.TRANSITION_DURATION)
                        }
                    )
            }
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
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = BottomSheetDefaults.TitleFontSize
                        )
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
                                    shape = RoundedCornerShape(ThemeDimensions.DefaultCornerRadius),
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
                        horizontal = BottomSheetDefaults.ActionsPaddingStart,
                        vertical = BottomSheetDefaults.DividerVerticalPadding))
                }

                // Actions
                Column(Modifier.padding(
                    start = BottomSheetDefaults.ActionsPaddingStart,
                    bottom = BottomSheetDefaults.ActionsBottomPadding
                )) {
                    if (currentData.shortcutActions.isNotEmpty()) {
                        currentData.shortcutActions.forEach { action ->
                            key(action.label) { ActionItem(action) }
                        }

                        HorizontalDivider(Modifier.padding(vertical = BottomSheetDefaults.DividerVerticalPadding))
                    }

                    currentData.actions.forEach { action ->
                        key(action.label) { ActionItem(action) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionItem(action: BottomSheetAppAction) {
    val alpha by animateFloatAsState(
        targetValue = if (action.isDimmed) ActionItemDefaults.DimmedAlpha else ActionItemDefaults.DefaultAlpha,
        label="dim_alpha"
    )
    val blur by animateDpAsState(
        targetValue = if (action.isDimmed) ActionItemDefaults.DimmedBlur else ActionItemDefaults.DefaultBlur,
        label="dim_blur"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .blur(blur)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (!action.isDimmed) Modifier.combinedClickable(onClick = action.onClick)
                        else Modifier
                )
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
                modifier = Modifier.padding(vertical = BottomSheetDefaults.ActionVerticalPadding),
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

        AnimatedVisibility(
            visible = action.isExpanded,
            enter = expandVertically(tween(BottomSheetDefaults.TRANSITION_DURATION)),
            exit = shrinkVertically(tween(BottomSheetDefaults.TRANSITION_DURATION))
        ) {
            if (action.expandedContent != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = ActionItemDefaults.VerticalPadding,
                            end = BottomSheetDefaults.ContentPadding
                        )
                ) {
                    action.expandedContent.invoke()
                }
            }
        }
    }
}
