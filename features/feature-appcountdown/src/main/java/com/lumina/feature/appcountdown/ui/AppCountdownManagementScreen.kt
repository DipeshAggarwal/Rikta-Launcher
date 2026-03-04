package com.lumina.feature.appcountdown.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.lumina.core.ui.components.settings.SettingsComponentsDefaults
import com.lumina.core.ui.components.settings.SettingsDivider
import com.lumina.core.ui.components.settings.SettingsHeader
import com.lumina.core.ui.components.settings.SettingsNavigationRow
import com.lumina.core.ui.components.settings.SettingsSegmentedButtonRow
import com.lumina.core.ui.components.settings.SettingsSpacer
import com.lumina.core.ui.components.settings.SettingsSubHeader
import com.lumina.feature.appcountdown.CountdownMode
import com.lumina.feature.appcountdown.CountdownSettingsViewModel
import com.lumina.feature.appcountdown.R

@Composable
fun AppCountdownManagementScreen(
    goToAppAdvancedSettingsCountdown: () -> Unit,
    goToAppPickerCountdown: () -> Unit,
    onBack: () -> Unit,
    viewModel: CountdownSettingsViewModel
) {
    val countdownSettings by viewModel.countdownSettings.collectAsStateWithLifecycle()

    var countdownDurationPerStep by remember(countdownSettings.countdownDurationPerStep) {
        mutableIntStateOf(countdownSettings.countdownDurationPerStep)
    }
    var countdownSteps by remember(countdownSettings.countdownSteps) {
        mutableIntStateOf(countdownSettings.countdownSteps)
    }
    var countdownWrapDuration by remember(countdownSettings.countdownWrapDuration) {
        mutableLongStateOf(countdownSettings.countdownWrapDuration)
    }
    val totalCountdownTime = (countdownDurationPerStep * countdownSteps) + (countdownWrapDuration * 0.001)

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        SettingsHeader(onBack, stringResource(R.string.countdown_apps))

        SettingsCardHeader(title = stringResource(R.string.total_countdown_time))
        SettingsCard {
            Text(
                text = stringResource(R.string.total_countdown_time_seconds, totalCountdownTime),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(
                        start = SettingsComponentsDefaults.HorizontalPadding,
                        top = SettingsComponentsDefaults.RowVerticalPadding
                    )
            )
            Text(
                text = "(Step Duration * Total Steps) + Wrap",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(
                        end = SettingsComponentsDefaults.HorizontalPadding,
                        bottom = SettingsComponentsDefaults.RowVerticalPadding
                    )
                    .align(Alignment.End)
            )
            SettingsDivider()

            SettingsSubHeader(stringResource(R.string.countdown_presets))
            SettingsSegmentedButtonRow(
                options = CountdownMode.entries,
                selectedOption = CountdownMode.fromValue(countdownDurationPerStep),
                onOptionSelected = { mode -> viewModel.setAllCountdownSliders(mode.value) },
                itemLabel = { stringResource(it.labelRes) }
            )
        }

        SettingsSpacer()

        SettingsCardHeader(title = stringResource(R.string.header_countdown_settings))
        SettingsCard {
            SettingsNavigationRow(
                label = stringResource(R.string.manage_countdown_apps),
                onClick = { goToAppPickerCountdown() }
            )
            SettingsDivider()
            SettingsNavigationRow(
                label = stringResource(R.string.tune_countdown_time),
                onClick = { goToAppAdvancedSettingsCountdown() }
            )
        }

        SettingsSpacer()
        SettingsSpacer()
    }
}
