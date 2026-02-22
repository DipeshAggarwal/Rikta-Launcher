package com.lumina.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lumina.core.ui.components.settings.SettingsButton
import com.lumina.core.ui.components.settings.SettingsHeader
import com.lumina.core.ui.components.settings.SettingsSlider
import com.lumina.core.ui.components.settings.SettingsSpacer
import com.lumina.feature.settings.R
import com.lumina.feature.settings.SettingsViewModel
import com.lumina.feature.settings.SpacerMode
import kotlin.math.roundToInt

@Composable
fun SpacerConfigScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel
) {
    val layoutSettings by viewModel.layoutSettings.collectAsState()
    val spacerHeight = layoutSettings.spacerHeight

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        item { SettingsHeader(onBack, stringResource(R.string.set_spacer_size)) }

        itemsIndexed(SpacerMode.entries) {index, mode ->
            SettingsButton(
                label = stringResource(mode.labelRes),
                isSelected = spacerHeight == mode.value,
                isTopOfGroup = index == 0,
                isBottomOfGroup = index == SpacerMode.entries.size - 1,
                onClick = {
                    viewModel.setSpacerHeight(mode.value)
                }
            )
        }

        item { SettingsSpacer() }

        item {
            SettingsSlider(
                label = stringResource(R.string.set_spacer_size_slider),
                value = spacerHeight.toFloat(),
                onValueChange = {
                    viewModel.setSpacerHeight(it.roundToInt())
                },
                valueRange = 5f..50f,
                steps = 8,
                onReset = {
                    viewModel.resetSpacerHeight()
                },
                isTopOfGroup = true,
                isBottomOfGroup = true
            )
        }

        item { SettingsSpacer() }
    }
}
