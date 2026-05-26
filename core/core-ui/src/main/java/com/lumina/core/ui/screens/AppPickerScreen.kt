package com.lumina.core.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumina.core.model.AppInfo
import com.lumina.core.model.componentKey
import com.lumina.core.ui.R
import com.lumina.core.ui.ReorderableListState
import com.lumina.core.ui.ThemeTokens
import com.lumina.core.ui.components.StandardListScaffold
import com.lumina.core.ui.components.TipBanner
import com.lumina.core.ui.theme.ContentColor
import com.lumina.core.ui.components.settings.SettingsSpacer
import com.lumina.core.ui.components.settings.animatedShape
import com.lumina.core.ui.components.settings.settingsGroupRadii

private val DragHandlePadding = 8.dp

@Composable
fun AppPickerScreen(
    apps: List<AppInfo>,
    preSelectedApps: List<String>,
    title: String,
    onBackClicked: () -> Unit,
    onAppClicked: (app: AppInfo, selected: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    listModifier: Modifier = Modifier,
    activeTitle: String = stringResource(R.string.allowed_apps),
    inactiveTitle: String = stringResource(R.string.other_apps),
    reorderable: Boolean = true,
    onAppMoved: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> }
) {
    // A buffer. Only needed for smooth animation.
    val localOrderSelection = remember { mutableStateListOf<String>() }
    val lazyListState = rememberLazyListState()

    val reorderState = remember {
        ReorderableListState { from, to ->
            val movedItem = localOrderSelection.removeAt(from)
            localOrderSelection.add(to, movedItem)
            onAppMoved(from, to)
        }
    }

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

            // Selected Apps
            val selectedApps = localOrderSelection.mapNotNull { appsMap[it] }
            items.add(ListItem.Header(activeTitle, selectedApps.size))
            selectedApps.forEach { items.add(ListItem.App(it, true))}

            items.add(ListItem.Spacer)

            // All remaining Apps
            val unselectedApps = apps.filter { it.componentKey !in localOrderSelection }
            items.add(ListItem.Header(inactiveTitle, unselectedApps.size))
            unselectedApps.forEach { items.add(ListItem.App(it, false))}

            items
        }
    }

    val firstSelectedPackage by remember { derivedStateOf { localOrderSelection.firstOrNull() } }
    val lastSelectedPackage by remember { derivedStateOf { localOrderSelection.lastOrNull() } }
    val firstUnselectedPackage by remember(apps) { derivedStateOf {
        apps.firstOrNull { it.componentKey !in localOrderSelection }?.componentKey }
    }
    val lastUnselectedPackage by remember(apps) { derivedStateOf {
        apps.lastOrNull { it.componentKey !in localOrderSelection }?.componentKey }
    }

    StandardListScaffold(
        title = title,
        onBack = onBackClicked,
        modifier = modifier,
        listModifier = listModifier,
        lazyListState = lazyListState,
        verticalColumnSpacing = ThemeTokens.Spacing.None
    ) {
        if (reorderable) {
            item {
                TipBanner(
                    tipText = stringResource(R.string.drag_to_reorder_tip),
                    iconVector = Icons.Outlined.DragHandle,
                    verticalSpacing = ThemeTokens.Spacing.Small
                )
            }
        }

        items(
            items = combinedItems,
            key = { item ->
                when (item) {
                    is ListItem.Header ->  "header_${item.title}"
                    is ListItem.App -> item.app.componentKey
                    ListItem.Spacer -> "spacer"
                }
            }
        ) { item ->
            when (item) {
                is ListItem.Header -> {
                    AppPickerSectionHeader(
                        title = item.title,
                        count = item.count,
                        modifier = Modifier.animateItem()
                    )
                }

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
                        val key = item.app.componentKey
                        val isDragging = reorderState.isDragging(key)
                        val currentIndex = localOrderSelection.indexOf(key)

                        AppPickerRow(
                            title = item.app.displayName,
                            selected = true,
                            onClick = { onAppClicked(item.app, item.selected) },
                            isTopOfGroup = isTopOfGroup,
                            isBottomOfGroup = isBottomOfGroup,
                            reorderable = true,
                            dragHandleModifier = reorderState.dragHandleModifier(
                                key = key,
                                currentIndex = currentIndex,
                                listSize = localOrderSelection.size
                            ),
                            modifier = reorderState.itemModifier(
                                key = key,
                                index = currentIndex,
                                totalCount = localOrderSelection.size
                            )
                        )
                    } else {
                        AppPickerRow(
                            title = item.app.displayName,
                            selected = item.selected,
                            onClick = { onAppClicked(item.app, item.selected) },
                            isTopOfGroup = isTopOfGroup,
                            isBottomOfGroup = isBottomOfGroup,
                            modifier = Modifier.animateItem()
                        )
                    }
                }

                ListItem.Spacer -> AnimatedVisibility(
                    visible = localOrderSelection.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                    modifier = Modifier.animateItem()
                ) {
                    SettingsSpacer()
                }
            }
        }
    }
}

@Composable
private fun AppPickerRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    isTopOfGroup: Boolean,
    isBottomOfGroup: Boolean,
    modifier: Modifier = Modifier,
    reorderable: Boolean = false,
    dragHandleModifier: Modifier = Modifier
) {
    val shape = settingsGroupRadii(isTopOfGroup, isBottomOfGroup).animatedShape()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(
                    horizontal = ThemeTokens.Spacing.Large,
                    vertical = ThemeTokens.Spacing.Medium
                )
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = ContentColor,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = ThemeTokens.Spacing.Medium)
            )

            when {
                selected && reorderable -> Icon(
                    imageVector = Icons.Outlined.DragHandle,
                    contentDescription = stringResource(R.string.drag_to_reorder),
                    tint = ContentColor.copy(alpha = ThemeTokens.Alpha.Medium),
                    modifier = dragHandleModifier
                )
                !selected -> Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.add_app),
                    tint = ContentColor.copy(alpha = ThemeTokens.Alpha.Medium)
                )
                else -> Unit
            }
        }

        // This is to ensure that the bottom edge is clean.
        if (!isBottomOfGroup) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = ThemeTokens.Spacing.ExtraLarge)
            )
        }
    }
}

@Composable
fun AppPickerSectionHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = ThemeTokens.Spacing.Small,
                vertical = ThemeTokens.Spacing.Medium
            )
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(ThemeTokens.Spacing.Medium))

        Box(
            modifier = Modifier
                .height(24.dp)
                .defaultMinSize(minWidth = 24.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = ThemeTokens.Spacing.Small),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

private sealed class ListItem {
    data class Header(val title: String, val count: Int) : ListItem()
    data class App(val app: AppInfo, val selected: Boolean) : ListItem()
    object Spacer : ListItem()
}
