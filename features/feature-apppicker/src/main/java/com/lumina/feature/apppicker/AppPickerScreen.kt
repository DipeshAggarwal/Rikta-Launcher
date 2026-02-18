package com.lumina.feature.apppicker

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
import com.lumina.core.ui.theme.ContentColor
import com.lumina.core.ui.components.settings.SettingsButton
import com.lumina.core.ui.components.settings.SettingsDefaults.TEXT_WEIGHT
import com.lumina.core.ui.components.settings.SettingsHeader
import com.lumina.core.ui.components.settings.SettingsSpacer
import com.lumina.domain.apps.AppInfo
import kotlin.math.roundToInt

private val DragHandlePadding = 8.dp

@Composable
fun AppPickerScreen (
    apps: List<AppInfo>,
    preSelectedApps: List<String>,
    title: String,
    onBackClicked: () -> Unit,
    onAppClicked: (app: AppInfo, selected: Boolean) -> Unit,
    reorderable: Boolean = false,
    hideTitle: Boolean = false,
    hideBack: Boolean = false,
    titleColor: Color = ContentColor,
    topPadding: Boolean = true,
    onAppMoved: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> }
) {
    // A buffer. Only needed for smooth animation.
    val localOrderSelection = remember { mutableStateListOf<String>() }
    val lazyListState = rememberLazyListState()

    var draggedPackageName by remember { mutableStateOf<String?>(null) }
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
            val appsMap = apps.associateBy { it.packageName }

            // All favourite Apps
            localOrderSelection.forEach { packageName ->
                appsMap[packageName]?.let { items.add(ListItem.App(it, true)) }
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
    val firstUnselectedPackage by remember(apps) { derivedStateOf { apps.firstOrNull()?.packageName } }
    val lastUnselectedPackage by remember(apps) { derivedStateOf { apps.lastOrNull()?.packageName } }

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
                    is ListItem.App -> "${if (item.selected) "selected" else "available"}_${item.app.packageName}"
                    ListItem.Spacer -> "spacer"
                }
            }
        ) { item ->
            when (item) {
                is ListItem.App -> {
                    val isTopOfGroup = if (item.selected) {
                        firstSelectedPackage == item.app.packageName
                    } else {
                        firstUnselectedPackage == item.app.packageName
                    }

                    val isBottomOfGroup = if (item.selected) {
                        lastSelectedPackage == item.app.packageName
                    } else {
                        lastUnselectedPackage == item.app.packageName
                    }

                    if (item.selected && reorderable) {
                        val isDragging = draggedPackageName == item.app.packageName
                        val currentIndex = localOrderSelection.indexOf(item.app.packageName)
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
                                    .pointerInput(item.app.packageName) {
                                        detectVerticalDragGestures(
                                            onDragStart = {
                                                draggedPackageName = item.app.packageName
                                                dragOffset = 0f
                                            },
                                            onDragEnd = {
                                                draggedPackageName = null
                                                dragOffset = 0f
                                            },
                                            onDragCancel = {
                                                draggedPackageName = null
                                                dragOffset = 0f
                                            }
                                        ) { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount

                                            val itemHeight = measuredItemHeight.toFloat()
                                            val threshold = itemHeight / 2

                                            val currentPkg = draggedPackageName ?: return@detectVerticalDragGestures

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
                            isDisabled = !item.selected && localOrderSelection.contains(item.app.packageName),
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
