package com.lumina.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.common.AppDefaults.DEFAULT_SPACER_HEIGHT
import com.lumina.domain.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
): ViewModel () {
    val spacerHeight: StateFlow<Int> = settingsRepository.spacerHeight()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            DEFAULT_SPACER_HEIGHT
        )

    fun setSpacerHeight(height: Int) {
        viewModelScope.launch {
            settingsRepository.setSpacerHeight(height)
        }
    }

    fun resetSpacerHeight() {
        viewModelScope.launch {
            settingsRepository.resetSpacerHeight()
        }
    }
}
