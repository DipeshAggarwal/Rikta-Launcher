package com.lumina.feature.appcountdown

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.domain.settings.CountdownSettings
import com.lumina.domain.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class CountdownViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<CountdownUiState>(CountdownUiState.Idle)
    val uiState: StateFlow<CountdownUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    fun start() {
        if (countdownJob != null) return

        _uiState.value = CountdownUiState.Idle
        countdownJob = viewModelScope.launch {
            val settings = settingsRepository.countdownSettings.first()
            runCountdown(settings)
        }
    }

    private suspend fun runCountdown(settings: CountdownSettings) {
        var step = settings.countdownSteps

        while (step > 0) {
            _uiState.value = CountdownUiState.Running(
                currentStep = step,
                showText = settings.showText,
                showNumber = true
            )
            delay(settings.countdownDurationPerStep * 1000L)

            _uiState.value = CountdownUiState.Running(
                currentStep = step,
                showText = settings.showText,
                showNumber = false
            )
            delay(500)
            step--
        }
        delay(settings.countdownWrapDuration)
        _uiState.value = CountdownUiState.Completed
        countdownJob = null
    }

    fun cancel() {
        countdownJob?.cancel()
        countdownJob = null
        _uiState.value = CountdownUiState.Cancelled
    }

    fun reset() {
        countdownJob?.cancel()
        countdownJob = null
        _uiState.value = CountdownUiState.Idle
    }
}
