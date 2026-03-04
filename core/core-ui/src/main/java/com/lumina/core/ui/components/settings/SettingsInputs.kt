package com.lumina.core.ui.components.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.R
import com.lumina.core.ui.components.text.AutoResizingText
import com.lumina.core.ui.theme.ContentColor
import com.lumina.core.ui.theme.LuminaCardDefaults
import com.lumina.core.ui.theme.SegmentSelectedColor
import com.lumina.core.ui.theme.SegmentSelectedContentColor
import com.lumina.core.ui.theme.TrackActiveColor

private object SettingsSliderDefaults {
    const val SLIDER_WEIGHT = 1.5f

    val SliderHorizontalPadding = 16.dp
    val ResetIconSize = 40.dp
}

private object SettingsSegmentedButtonsDefaults {
    const val SEGMENTED_ROW_WEIGHT = 4f
    val RowStartPadding = 16.dp
}

private object SettingsSwitchRowDefaults {
    val TextEndPadding = 16.dp
}

private object SettingsSegmentedButtonRowDefaults {
    const val SLIDE_TRANSITION_DURATION = 200
    val RowAlpha = 0.75f
    val DividerAlpha = 0.2f
    val SegmentBoxPadding = 4.dp
    val HorizontalPadding = 16.dp
    val VerticalPadding = 8.dp
    val DividerWidth = 0.5.dp
    val DividerHeight = 16.dp
}

/**
 * Switch for setting with a label on the left
 *
 * @param label The text for the label
 * @param checked Whether the switch is on or not
 * @param onCheckedChange Function with Boolean passed that's executed when the switch is pressed
 */
@Composable
fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false
) {
    var isChecked by remember { mutableStateOf(checked) }

    val currentShape = settingsGroupShape(
        isTopOfGroup = isTopOfGroup,
        isBottomOfGroup = isBottomOfGroup
    )

    Card(
        modifier = Modifier
            .padding(vertical = SettingsComponentsDefaults.VerticalPadding )
            .clip(currentShape)
            .clickable {
                isChecked = !isChecked
                onCheckedChange(isChecked)
            },shape = currentShape,
        colors = LuminaCardDefaults.colors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SettingsComponentsDefaults.HorizontalPadding,
                    vertical = SettingsComponentsDefaults.VerticalContentPadding
                )
                .height(SettingsComponentsDefaults.ContentHeight), verticalAlignment = Alignment.CenterVertically
        ) {
            AutoResizingText(
                text = label,
                modifier = Modifier
                    .weight(SettingsComponentsDefaults.TEXT_WEIGHT)
                    .padding(end = SettingsComponentsDefaults.TextIconSpacing), // Add space between text and switch
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = isChecked, onCheckedChange = {
                    isChecked = it
                    onCheckedChange(isChecked)
                })
        }
    }
}

/**
 * A setting item with a label on the left, a Slider in the middle, and a reset Icon on the right.
 * Visually styled to match SettingsSingleChoiceSegmentedButtons.
 *
 * @param label The text for the label displayed to the left of the slider.
 * @param value The current value of the slider.
 * @param onValueChange Callback that is invoked when the slider's value changes.
 * @param valueRange The range of values that the slider can take.
 * @param steps The number of discrete steps the slider can snap to between the min and max values.
 * @param onReset Callback that is invoked when the reset icon is clicked.
 * @param modifier Optional [Modifier] for this composable.
 * @param isTopOfGroup Whether this item is the first in a group of settings, for corner rounding.
 * @param isBottomOfGroup Whether this item is the last in a group of settings, for corner rounding.
 */
@Composable
fun SettingsSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false
) {
    val currentShape = settingsGroupShape(
        isTopOfGroup = isTopOfGroup,
        isBottomOfGroup = isBottomOfGroup
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SettingsComponentsDefaults.VerticalPadding),
        shape = currentShape,
        colors = LuminaCardDefaults.colors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SettingsComponentsDefaults.HorizontalPadding,
                    vertical = SettingsComponentsDefaults.VerticalContentPadding
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AutoResizingText(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(SettingsComponentsDefaults.TEXT_WEIGHT)
            )

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                modifier = Modifier
                    .weight(SettingsSliderDefaults.SLIDER_WEIGHT)
                    .padding(horizontal = SettingsSliderDefaults.SliderHorizontalPadding)
            )

            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.reset_to_default),
                modifier = Modifier
                    .size(SettingsSliderDefaults.ResetIconSize)
                    .padding(start = SettingsComponentsDefaults.TextIconSpacing)
                    .clickable(onClick = onReset),
                tint = ContentColor,
            )
        }
    }
}

/**
 * A setting item with a label on the left and a SingleChoiceSegmentedButtonRow on the right.
 *
 * @param label The text for the label.
 * @param options A list of strings representing the choices for the segmented buttons.
 * @param selectedIndex The currently selected index in the options list.
 * @param onSelectedIndexChange Callback that is invoked when the selection changes.
 * @param isTopOfGroup Whether this item is the first in a group of settings.
 * @param isBottomOfGroup Whether this item is the last in a group of settings.
 */
@Composable
fun SettingsSingleChoiceSegmentedButtons(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false
) {
    var currentSelectedIndex by remember { mutableIntStateOf(selectedIndex) }

    val currentShape = settingsGroupShape(
        isTopOfGroup = isTopOfGroup,
        isBottomOfGroup = isBottomOfGroup
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SettingsComponentsDefaults.VerticalPadding),
        shape = currentShape,
        colors = LuminaCardDefaults.colors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SettingsComponentsDefaults.HorizontalPadding,
                    vertical = SettingsComponentsDefaults.VerticalContentPadding
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AutoResizingText(
                text = label,
                modifier = Modifier.weight(SettingsComponentsDefaults.TEXT_WEIGHT)
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .padding(start = SettingsSegmentedButtonsDefaults.RowStartPadding)
                    .weight(SettingsSegmentedButtonsDefaults.SEGMENTED_ROW_WEIGHT)
            ) {
                options.forEachIndexed { index, optionLabel ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index, count = options.size
                        ), onClick = {
                            currentSelectedIndex = index
                            onSelectedIndexChange(index)
                        }, selected = index == currentSelectedIndex
                    ) {
                        Text(
                            text = optionLabel,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = SettingsComponentsDefaults.MAX_LINES
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = SettingsComponentsDefaults.HorizontalPadding,
                vertical = SettingsComponentsDefaults.RowVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = ContentColor,
            maxLines = SettingsComponentsDefaults.MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(SettingsComponentsDefaults.TEXT_WEIGHT)
                .padding(SettingsSwitchRowDefaults.TextEndPadding)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun <T> SettingsSegmentedButtonRow(
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemLabel: @Composable (T) -> String = { it.toString() }
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = SettingsComponentsDefaults.HorizontalPadding,
                vertical = SettingsComponentsDefaults.RowVerticalPadding
            )
            .clip(MaterialTheme.shapes.extraLarge)
            .background(TrackActiveColor.copy(alpha = SettingsSegmentedButtonRowDefaults.RowAlpha))
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selectedOption
            val isNextSelected = options.getOrNull(index + 1) == selectedOption

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) SegmentSelectedColor else Color.Transparent,
                animationSpec = tween(SettingsSegmentedButtonRowDefaults.SLIDE_TRANSITION_DURATION),
                label = "segment_bg_$index"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) SegmentSelectedContentColor
                    else ContentColor.copy(alpha = SettingsSegmentedButtonRowDefaults.RowAlpha),
                animationSpec = tween(SettingsSegmentedButtonRowDefaults.SLIDE_TRANSITION_DURATION),
                label = "segment_content_$index"
            )

            Box(
                modifier = Modifier
                    .weight(SettingsComponentsDefaults.TEXT_WEIGHT)
                    .padding(SettingsSegmentedButtonRowDefaults.SegmentBoxPadding)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(bgColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onOptionSelected(option) }
                    .padding(
                        horizontal = SettingsSegmentedButtonRowDefaults.HorizontalPadding,
                        vertical = SettingsSegmentedButtonRowDefaults.VerticalPadding
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = itemLabel(option),
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = SettingsComponentsDefaults.MAX_LINES,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (index < options.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(SettingsSegmentedButtonRowDefaults.DividerWidth)
                        .height(SettingsSegmentedButtonRowDefaults.DividerHeight)
                        .align(Alignment.CenterVertically)
                        .background(
                            color = if (!isSelected && !isNextSelected)
                                ContentColor.copy(alpha = SettingsSegmentedButtonRowDefaults.DividerAlpha)
                            else
                                Color.Transparent
                        )
                )
            }
        }
    }
}
