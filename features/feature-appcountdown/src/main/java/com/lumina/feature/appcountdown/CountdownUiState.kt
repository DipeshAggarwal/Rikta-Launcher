package com.lumina.feature.appcountdown

sealed interface CountdownUiState {
    data class Running(
        val currentStep: Int,
        val showText: Boolean,
        val showNumber: Boolean
    ) : CountdownUiState
    data object Idle : CountdownUiState
    data object Completed : CountdownUiState
    data object Cancelled : CountdownUiState
}
