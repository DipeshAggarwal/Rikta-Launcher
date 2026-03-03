package com.lumina.feature.appcountdown.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumina.core.ui.components.settings.SettingsCard
import com.lumina.core.ui.components.settings.SettingsDivider
import com.lumina.core.ui.components.settings.SettingsHeader
import com.lumina.core.ui.components.settings.SettingsNavigationRow
import com.lumina.core.ui.components.settings.SettingsSegmentedButtonRow
import com.lumina.core.ui.components.settings.SettingsSliderRow
import com.lumina.core.ui.components.settings.SettingsSpacer
import com.lumina.core.ui.components.settings.SettingsSubHeader
import com.lumina.feature.appcountdown.CountdownMode
import com.lumina.feature.appcountdown.CountdownSettingsViewModel
import com.lumina.feature.appcountdown.R
import kotlin.math.roundToInt

@Composable
fun CountdownAppsManagementScreen(
    goToAppPickerCountdown: () -> Unit,
    onBack: () -> Unit,
    viewModel: CountdownSettingsViewModel
) {
    val countdownApps by viewModel.countdownApps.collectAsStateWithLifecycle()
    val countdownSettings by viewModel.countdownSettings.collectAsStateWithLifecycle()

    var countdownDurationPerStep by remember(countdownSettings.countdownDurationPerStep) {
        mutableIntStateOf(countdownSettings.countdownDurationPerStep)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        SettingsHeader(onBack, stringResource(R.string.countdown_apps))

        SettingsCard {
            SettingsNavigationRow(
                label = stringResource(R.string.manage_countdown_apps),
                onClick = { goToAppPickerCountdown() }
            )
        }

        SettingsSpacer()

        SettingsCard {
            SettingsSubHeader(stringResource(R.string.countdown_duration_per_step))

            SettingsSegmentedButtonRow(
                options = CountdownMode.entries,
                selectedOption = CountdownMode.fromValue(countdownDurationPerStep),
                onOptionSelected = { mode -> viewModel.setCountdownDurationPerStep(mode.value) },
                itemLabel = { stringResource(it.labelRes) }
            )
            SettingsDivider()

            SettingsSliderRow(
                label = stringResource(R.string.set_app_countdown_time_slider),
                value = countdownDurationPerStep.toFloat(),
                valueRange = 1f..5f,
                onValueChange = { countdownDurationPerStep = it.toInt() },
                onValueChangeFinished = { viewModel.setCountdownDurationPerStep(countdownDurationPerStep) },
                displayName = countdownDurationPerStep.toString(),
                onReset = { viewModel.resetCountdownDurationPerStep()}
            )
        }
    }
}
