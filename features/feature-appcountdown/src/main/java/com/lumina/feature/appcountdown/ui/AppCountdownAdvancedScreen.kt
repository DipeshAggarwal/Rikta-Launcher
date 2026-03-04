package com.lumina.feature.appcountdown.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumina.core.ui.components.settings.SettingsCard
import com.lumina.core.ui.components.settings.SettingsCardHeader
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

@Composable
fun AppCountdownAdvancedScreen(
    onBack: () -> Unit,
    viewModel: CountdownSettingsViewModel
) {
    val countdownSettings by viewModel.countdownSettings.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var countdownDurationPerStep by remember(countdownSettings.countdownDurationPerStep) {
        mutableIntStateOf(countdownSettings.countdownDurationPerStep)
    }
    var countdownSteps by remember(countdownSettings.countdownSteps) {
        mutableIntStateOf(countdownSettings.countdownSteps)
    }
    var countdownWrapDuration by remember(countdownSettings.countdownWrapDuration) {
        mutableLongStateOf(countdownSettings.countdownWrapDuration)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        SettingsHeader(onBack, stringResource(R.string.tune_countdown_time))

        SettingsCardHeader(stringResource(R.string.manage_countdown_setting))
        SettingsCard {
            SettingsSubHeader(stringResource(R.string.countdown_duration_per_step))
            SettingsSliderRow(
                label = stringResource(R.string.app_countdown_seconds_per_step),
                value = countdownDurationPerStep.toFloat(),
                valueRange = 1f..5f,
                onValueChange = { countdownDurationPerStep = it.toInt() },
                onValueChangeFinished = { viewModel.setCountdownDurationPerStep(countdownDurationPerStep) },
                displayName = countdownDurationPerStep.toString(),
                onReset = { viewModel.resetCountdownDurationPerStep()}
            )
            SettingsDivider()

            SettingsSubHeader(stringResource(R.string.countdown_total_steps))
            SettingsSliderRow(
                label = stringResource(R.string.set_app_countdown_steps_slider),
                value = countdownSteps.toFloat(),
                valueRange = 4f..8f,
                onValueChange = { countdownSteps = it.toInt() },
                onValueChangeFinished = { viewModel.setCountdownSteps(countdownSteps) },
                displayName = countdownSteps.toString(),
                onReset = { viewModel.resetCountdownSteps()}
            )
            SettingsDivider()

            SettingsSubHeader(stringResource(R.string.countdown_duration_wrap))
            SettingsSliderRow(
                label = stringResource(R.string.app_countdown_duration_slider),
                value = (countdownWrapDuration * 0.01).toFloat(),
                valueRange = 5f..10f,
                onValueChange = { countdownWrapDuration = (it * 100f).toLong() },
                onValueChangeFinished = { viewModel.setCountdownWrapDuration(countdownWrapDuration) },
                displayName = (countdownWrapDuration * 0.001).toString(),
                onReset = { viewModel.resetCountdownWrapDuration()}
            )
        }

        SettingsSpacer()
        SettingsSpacer()
    }
}
