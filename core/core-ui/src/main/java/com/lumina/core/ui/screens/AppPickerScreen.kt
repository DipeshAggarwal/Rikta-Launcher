package com.lumina.core.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.lumina.core.model.AppInfo
import com.lumina.core.model.componentKey
import com.lumina.core.ui.R
import com.lumina.core.ui.theme.ContentColor
import com.lumina.core.ui.components.settings.SettingsButton
import com.lumina.core.ui.components.settings.SettingsComponentsDefaults.TEXT_WEIGHT
import com.lumina.core.ui.components.settings.SettingsHeader
import com.lumina.core.ui.components.settings.SettingsSpacer
import kotlin.math.roundToInt

private val DragHandlePadding = 8.dp

@Composable
fun AppPickerScreen (
    apps: List<AppInfo>,
    preSelectedApps: List<String>,
    title: String,
    onBackClicked: () -> Unit,
    onAppClicked: (app: AppInfo, selected: Boolean) -> Unit,
    hideTitle: Boolean = false,
    hideBack: Boolean = false,
    titleColor: Color = ContentColor,
    topPadding: Boolean = true,
    reorderable: Boolean = false,
    onAppMoved: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> }
) {
    // A buffer. Only needed for smooth animation.
    val localOrderSelection = remember { mutableStateListOf<String>() }
    val lazyListState = rememberLazyListState()

    var draggedComponentKey by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var measuredItemHeight by remember { mutableIntStateOf(0) }

    // Ensures that the ViewModel and local buffer is synced.
    LaunchedEffect(preSelectedApps) {
        if (localOrderSelection.toList() != preSelectedApps) {
            localOrderSelection.clear()
            localOrderSelection.addAll(preSelectedApps)
        }
    }

    // Create a combined list with a spacer marker.
    // Only dependency is apps because localOrderSelection is a [SnapshotStateList] which is tracked
    // automagically by derivedStateOf
    val combinedItems by remember(apps) {
        derivedStateOf {
            val items = mutableListOf<ListItem>()
            val appsMap = apps.associateBy { it.componentKey }

            // All favourite Apps
            localOrderSelection.forEach { componentKey ->
                appsMap[componentKey]?.let { items.add(ListItem.App(it, true)) }
            }

            if (localOrderSelection.isNotEmpty())  items.add(ListItem.Spacer)

            // All remaining Apps
            apps.forEach { appInfo ->
                items.add(ListItem.App(appInfo, false))
            }
            items
        }
    }

    val firstSelectedPackage by remember { derivedStateOf { localOrderSelection.firstOrNull() } }
    val lastSelectedPackage by remember { derivedStateOf { localOrderSelection.lastOrNull() } }
    val firstUnselectedPackage by remember(apps) { derivedStateOf { apps.firstOrNull()?.componentKey } }
    val lastUnselectedPackage by remember(apps) { derivedStateOf { apps.lastOrNull()?.componentKey } }

    LazyColumn(
        state = lazyListState,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        if (!hideTitle) {
            item {
                SettingsHeader(
                    goBack = { onBackClicked() },
                    title = title,
                    hideBack = hideBack,
                    color = titleColor,
                    padding = topPadding
                )
            }
        }

        items(
            items = combinedItems,
            key = { item ->
                when (item) {
                    is ListItem.App -> "${if (item.selected) "selected" else "available"}_${item.app.componentKey}"
                    ListItem.Spacer -> "spacer"
                }
            }
        ) { item ->
            when (item) {
                is ListItem.App -> {
                    val isTopOfGroup = if (item.selected) {
                        firstSelectedPackage == item.app.componentKey
                    } else {
                        firstUnselectedPackage == item.app.componentKey
                    }

                    val isBottomOfGroup = if (item.selected) {
                        lastSelectedPackage == item.app.componentKey
                    } else {
                        lastUnselectedPackage == item.app.componentKey
                    }

                    if (item.selected && reorderable) {
                        val isDragging = draggedComponentKey == item.app.componentKey
                        val currentIndex = localOrderSelection.indexOf(item.app.componentKey)
                        val maxDragUp = -currentIndex * measuredItemHeight.toFloat()
                        val maxDragDown = (localOrderSelection.size - 1 - currentIndex) * measuredItemHeight.toFloat()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onSizeChanged { size ->
                                    if (measuredItemHeight == 0) measuredItemHeight = size.height
                                }
                                .zIndex(if (isDragging) 1f else 0f)
                                // Manually animate it this item is being dragged otherwise let
                                // Compose handle the animation.
                                .then(if (!isDragging) Modifier.animateItem() else Modifier)
                                .offset {
                                    IntOffset(
                                        x = 0,
                                        y = if (isDragging) {
                                            dragOffset.coerceIn(maxDragUp, maxDragDown).roundToInt()
                                        } else 0
                                    )
                                }
                        ) {
                            SettingsButton(
                                label = item.app.displayName,
                                onClick = {
                                    onAppClicked(item.app, item.selected)
                                },
                                isTopOfGroup = isTopOfGroup,
                                isBottomOfGroup = isBottomOfGroup,
                                modifier = Modifier.weight(TEXT_WEIGHT)
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = DragHandlePadding)
                                    .pointerInput(item.app.componentKey) {
                                        detectVerticalDragGestures(
                                            onDragStart = {
                                                draggedComponentKey = item.app.componentKey
                                                dragOffset = 0f
                                            },
                                            onDragEnd = {
                                                draggedComponentKey = null
                                                dragOffset = 0f
                                            },
                                            onDragCancel = {
                                                draggedComponentKey = null
                                                dragOffset = 0f
                                            }
                                        ) { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount

                                            val itemHeight = measuredItemHeight.toFloat()
                                            val threshold = itemHeight / 2

                                            val currentPkg = draggedComponentKey ?: return@detectVerticalDragGestures

                                            val fromIndex = localOrderSelection.indexOf(currentPkg)
                                            if (fromIndex == -1) return@detectVerticalDragGestures

                                            if (dragOffset > threshold && fromIndex < localOrderSelection.size - 1) {
                                                val toIndex = fromIndex + 1
                                                val movedItem = localOrderSelection.removeAt(fromIndex)
                                                localOrderSelection.add(toIndex, movedItem)

                                                dragOffset -= itemHeight
                                                onAppMoved(fromIndex, toIndex)
                                            } else if (dragOffset < -threshold && fromIndex > 0) {
                                                val toIndex = fromIndex - 1
                                                val movedItem = localOrderSelection.removeAt(fromIndex)
                                                localOrderSelection.add(toIndex, movedItem)

                                                dragOffset += itemHeight
                                                onAppMoved(fromIndex, toIndex)
                                            }
                                        }
                                    }
                            ) {
                                Icon(
                                    Icons.Default.DragHandle,
                                    contentDescription = stringResource(R.string.drag_to_reorder),
                                    tint = ContentColor
                                )
                            }
                        }
                    } else {
                        SettingsButton(
                            label = item.app.displayName,
                            onClick = {
                                onAppClicked(item.app, item.selected)
                            },
                            isTopOfGroup = isTopOfGroup,
                            isBottomOfGroup = isBottomOfGroup,
                            isDisabled = !item.selected && localOrderSelection.contains(item.app.componentKey),
                            modifier = Modifier.animateItem()
                        )
                    }
                }

                ListItem.Spacer -> AnimatedVisibility(
                    visible = localOrderSelection.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    SettingsSpacer()
                }
            }
        }

        item {
            SettingsSpacer()
        }
    }
}

private sealed class ListItem {
    data class App(val app: AppInfo, val selected: Boolean) : ListItem()
    object Spacer : ListItem()
}
