package com.lumina.feature.appcountdown.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumina.core.ui.HapticUtils
import com.lumina.core.ui.components.settings.SettingsButton
import com.lumina.core.ui.components.settings.SettingsHeader
import com.lumina.core.ui.components.settings.SettingsSlider
import com.lumina.core.ui.components.settings.SettingsSpacer
import com.lumina.core.ui.components.settings.SettingsSubheading
import com.lumina.core.ui.components.settings.SettingsSwipeableButton
import com.lumina.core.ui.components.settings.SettingsSwitch
import com.lumina.feature.appcountdown.CountdownMode
import com.lumina.feature.appcountdown.CountdownSettingsViewModel
import com.lumina.feature.appcountdown.R

@Composable
fun CountdownAppsManagementScreen(
    goToAppPickerCountdown: () -> Unit,
    onBack: () -> Unit,
    viewModel: CountdownSettingsViewModel
) {
    val countdownApps by viewModel.countdownApps.collectAsStateWithLifecycle()
    val countdownSettings by viewModel.countdownSettings.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current

    var countdownDurationPerStep by remember { mutableIntStateOf(countdownSettings.countdownDurationPerStep) }
    var countdownSteps by remember { mutableIntStateOf(countdownSettings.countdownSteps) }
    var countdownWrapDuration = countdownSettings.countdownWrapDuration
    var showText = countdownSettings.showText

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SettingsHeader(onBack, stringResource(R.string.countdown_apps))
        }
        item {
            SettingsButton(
                label = stringResource(R.string.manage_countdown_apps),
                isTopOfGroup = true,
                isBottomOfGroup = true,
                onClick = {
                    goToAppPickerCountdown()
                }
            )
        }

        item { SettingsSubheading(stringResource(id = R.string.countdown_duration_per_step)) }

        itemsIndexed(CountdownMode.entries) { index, mode ->
            SettingsButton(
                label = stringResource(mode.labelRes),
                isSelected = countdownDurationPerStep == mode.value,
                isTopOfGroup = index == 0,
                isBottomOfGroup = index == CountdownMode.entries.size - 1,
                onClick = {
                    countdownDurationPerStep = mode.value
                    viewModel.setCountdownDurationPerStep(mode.value)
                }
            )
        }

        item { SettingsSpacer() }

        item {
            SettingsSlider(
                label = stringResource(R.string.set_app_countdown_time_slider),
                value = countdownDurationPerStep.toFloat(),
                onValueChange = {
                    countdownDurationPerStep = it.toInt()
                    viewModel.setCountdownDurationPerStep(countdownDurationPerStep)
                },
                valueRange = 1f..5f,
                steps = 3,
                onReset = {
                    countdownDurationPerStep = viewModel.resetAndGetCountdownDurationPerStep()
                },
                isTopOfGroup = true,
                isBottomOfGroup = true
            )
        }

        item { SettingsSubheading(stringResource(id = R.string.countdown_total_steps)) }
        item {
            SettingsSlider(
                label = stringResource(R.string.set_app_countdown_steps_slider),
                value = countdownSteps.toFloat(),
                onValueChange = {
                    countdownSteps = it.toInt()
                    viewModel.setCountdownSteps(countdownSteps)
                },
                valueRange = 1f..5f,
                steps = 3,
                onReset = {
                    countdownSteps = viewModel.resetAndGetCountdownSteps()
                },
                isTopOfGroup = true,
                isBottomOfGroup = true
            )
        }

        item { SettingsSubheading(stringResource(id = R.string.countdown_show_text)) }

        item {
            SettingsSwitch(
                label = stringResource(R.string.countdown_show_text),
                checked = showText,
                onCheckedChange = { viewModel.setShowText(it) },
                isTopOfGroup = true,
                isBottomOfGroup = true,
            )
        }

//        item {
//            SettingsSubheading()
//        }
//
//        items(
//            items = countdownApps,
//            key = { app -> app.packageName }
//        ) { app ->
//            SettingsSwipeableButton(
//                modifier = Modifier.animateItem(), // Smoothly handles removal animations.
//                label = app.displayName,
//                onClick = {},
//                onDeleteClick = {
//                    HapticUtils.performHapticFeedback(haptics)
//                    viewModel.removeCountdownApp(app.packageName)
//                },
//                isTopOfGroup = countdownApps.firstOrNull() == app,
//                isBottomOfGroup = countdownApps.lastOrNull() == app
//            )
//        }
//
//        item { SettingsSpacer() }
    }
}
