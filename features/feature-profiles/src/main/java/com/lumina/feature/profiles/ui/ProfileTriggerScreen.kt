package com.lumina.feature.profiles.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumina.core.model.LogicalOperator
import com.lumina.core.model.ProfileTriggerType
import com.lumina.core.ui.ReorderableListState
import com.lumina.core.ui.ThemeTokens
import com.lumina.core.ui.components.ElevatedButton
import com.lumina.core.ui.components.StandardListScaffold
import com.lumina.core.ui.components.TipCarousel
import com.lumina.core.ui.components.TipContent
import com.lumina.domain.profiles.model.TriggerCondition
import com.lumina.feature.profiles.ProfileTriggersListEvent
import com.lumina.feature.profiles.ProfileTriggersListViewModel
import com.lumina.feature.profiles.R
import com.lumina.feature.profiles.extensions.displayName
import com.lumina.feature.profiles.extensions.subtitle
import com.lumina.feature.profiles.ui.bottomsheet.TriggerTypePickerBottomSheet
import kotlinx.coroutines.launch
import com.lumina.core.ui.R as uiR

@Composable
fun ProfileTriggerScreen(
    viewModel: ProfileTriggersListViewModel = hiltViewModel(),
    onNavigateToEditTrigger: (Long) -> Unit,
    onNavigateToAddTrigger: (ProfileTriggerType) -> Unit,
    onBack: () -> Unit
) {
    val triggers by viewModel.triggers.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val localTriggers = remember { mutableStateListOf<TriggerCondition>() }
    LaunchedEffect(triggers) {
        if (localTriggers != triggers) {
            localTriggers.clear()
            localTriggers.addAll(triggers)
        }
    }

    var bottomSheetTarget by remember { mutableStateOf<TriggerCondition?>(null) }
    var operatorTarget by remember { mutableStateOf<TriggerCondition?>(null) }
    var showTypePicker by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        launch {
            viewModel.events.collect { event ->
                when (event) {
                    is ProfileTriggersListEvent.NavigateToEditTrigger ->
                        onNavigateToEditTrigger(event.triggerId)
                }
            }
        }
        launch {
            viewModel.errorMessage.collect { snackbarHostState.showSnackbar(it) }
        }
    }

    StandardListScaffold(
        title = stringResource(R.string.profile_trigger_heading),
        onBack = onBack,
        snackbarHostState = snackbarHostState
    ) {
        item(key = "profile_trigger_subtitle") {
            Text(
                text = stringResource(R.string.profile_trigger_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (localTriggers.isNotEmpty()) {
            item(key = "profile_trigger_list") {
                TriggerSequenceList(
                    triggers = localTriggers,
                    onLongPress = { trigger -> bottomSheetTarget = trigger },
                    onOperatorClick = { nextTrigger -> operatorTarget = nextTrigger },
                    onReorder = { prev, next ->
                        val from = localTriggers[prev]
                        val to = localTriggers[next]

                        localTriggers.removeAt(prev)
                        localTriggers.add(next, from)
                        viewModel.onReorder(from, to)
                    }
                )
            }
        } else {
            item(key = "profile_trigger_empty_list") {
                Spacer(modifier = Modifier.height(ThemeTokens.Spacing.ExtraLarge))
                Text(
                    text = stringResource(R.string.profile_trigger_no_trigger),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = ThemeTokens.Spacing.ExtraLarge
                        )
                )
                Spacer(modifier = Modifier.height(ThemeTokens.Spacing.ExtraLarge))
            }
        }

        item(key = "profile_trigger_tips_carousel") {
            TipCarousel(
                tips = listOf(
                    TipContent(
                        text = stringResource(R.string.profile_trigger_tip_1),
                        icon = Icons.Outlined.Info
                    ),
                    TipContent(
                        text = stringResource(R.string.profile_trigger_tip_2),
                        icon = Icons.Outlined.Info
                    ),
                    TipContent(
                        text = stringResource(R.string.profile_trigger_tip_3),
                        icon = Icons.Outlined.Info
                    ),
                    TipContent(
                        text = stringResource(R.string.profile_trigger_tip_4),
                        icon = Icons.Outlined.Info
                    ),
                    TipContent(
                        text = stringResource(R.string.profile_trigger_tip_5),
                        icon = Icons.Outlined.Info
                    )
                )
            )
        }

        item(key = "profile_trigger_add") {
            ElevatedButton(
                title = stringResource(R.string.profile_trigger_add_trigger),
                subtitle = null,
                enabled = true,
                leadingIcon = Icons.Outlined.Add,
                trailingIcon = null,
                onClick = { showTypePicker = true }
            )
        }
    }

    if (showTypePicker) {
        TriggerTypePickerBottomSheet(
            onSelect = { type ->
                showTypePicker = false
                onNavigateToAddTrigger(type)
            },
            onDismiss = { showTypePicker = false }
        )
    }
}

@Composable
fun TriggerSequenceList(
    triggers: List<TriggerCondition>,
    onLongPress: (TriggerCondition) -> Unit,
    onOperatorClick: (TriggerCondition) -> Unit,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit
) {
    val reorderState = remember { ReorderableListState(onReorder) }

    Column {
        triggers.forEachIndexed { index, trigger ->
            val key = trigger.triggerId.toString()
            val isDragging = reorderState.isDragging(key)
            val nextTrigger = triggers.getOrNull(index + 1)

            TriggerSequenceItem(
                trigger = trigger,
                number = index + 1,
                triggerOperator = nextTrigger?.logicalOperator,
                hasNext = nextTrigger != null,
                isDragging = isDragging,
                dragModifier = reorderState.dragHandleModifier(
                    key = key,
                    currentIndex = index,
                    listSize = triggers.size
                ),
                itemModifier = reorderState.itemModifier(
                    key = key,
                    index = index,
                    totalCount = triggers.size
                ),
                onLongPress = { onLongPress(trigger) },
                onOperatorClick = { if (nextTrigger != null) onOperatorClick(nextTrigger) }
            )
        }
    }
}

@Composable
fun TriggerSequenceItem(
    trigger: TriggerCondition,
    number: Int,
    triggerOperator: LogicalOperator?,
    hasNext: Boolean,
    isDragging: Boolean,
    dragModifier: Modifier,
    itemModifier: Modifier,
    onLongPress: () -> Unit,
    onOperatorClick: () -> Unit
) {
    Row(
        modifier = itemModifier.fillMaxWidth().height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp).fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(ThemeTokens.Spacing.Medium))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ThemeTokens.Spacing.Small)
        ) {
            TriggerCard(
                trigger = trigger,
                isDragging = isDragging,
                dragModifier = dragModifier,
                onLongPress = onLongPress
            )
            if (hasNext) OperatorChip(triggerOperator = triggerOperator, onClick = onOperatorClick)
            Spacer(modifier = Modifier.height(ThemeTokens.Spacing.Small))
        }
    }
}

@Composable
private fun TriggerCard(
    trigger: TriggerCondition,
    isDragging: Boolean,
    dragModifier: Modifier,
    onLongPress: () -> Unit
) {
    val elevation = if (isDragging) 4.dp else 0.dp

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onLongClick = onLongPress, onClick = {})
                .padding(
                    start = ThemeTokens.Spacing.Large,
                    end = ThemeTokens.Spacing.Medium,
                    top = ThemeTokens.Spacing.Medium,
                    bottom = ThemeTokens.Spacing.Medium,
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trigger.triggerType.displayName(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = trigger.triggerType.subtitle(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Outlined.DragHandle,
                contentDescription = stringResource(uiR.string.drag_to_reorder),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = dragModifier.size(ThemeTokens.Icon.TertiaryIconSize)
            )
        }
    }
}

@Composable
private fun OperatorChip(
    triggerOperator: LogicalOperator?,
    onClick: () -> Unit
) {
    val isNot = triggerOperator == LogicalOperator.NOT
    val isUnset = triggerOperator == null

    val border = when {
        isNot -> MaterialTheme.colorScheme.secondary
        isUnset -> MaterialTheme.colorScheme.outlineVariant
        else -> MaterialTheme.colorScheme.primary
    }
    val container = when {
        isNot -> MaterialTheme.colorScheme.secondaryContainer
        isUnset -> MaterialTheme.colorScheme.surfaceContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val content = when {
        isNot -> MaterialTheme.colorScheme.onSecondaryContainer
        isUnset -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    val chipText = when (triggerOperator) {
        LogicalOperator.AND -> stringResource(R.string.profile_trigger_logical_operator_and)
        LogicalOperator.OR -> stringResource(R.string.profile_trigger_logical_operator_or)
        LogicalOperator.NOT -> stringResource(R.string.profile_trigger_logical_operator_not)
        null -> stringResource(R.string.profile_trigger_logical_operator_empty)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, border, RoundedCornerShape(50))
            .background(container)
            .clickable(onClick = onClick)
            .padding(
                horizontal = ThemeTokens.Spacing.Large,
                vertical = ThemeTokens.Spacing.Small
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ThemeTokens.Spacing.Small)
    ) {
        Text(
            text = chipText,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = content
        )
        Icon(
            imageVector = Icons.Outlined.ExpandMore,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(ThemeTokens.Icon.BannerCloseIconSize)
        )
    }
}
