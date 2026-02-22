package com.lumina.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.domain.settings.LayoutSettings
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
    val layoutSettings: StateFlow<LayoutSettings> = settingsRepository.layoutSettings
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            LayoutSettings()
        )

    fun setSpacerHeight(height: Int) {
        viewModelScope.launch {
            settingsRepository.updateLayoutSettings {
                copy(spacerHeight = height)
            }
        }
    }

    fun resetSpacerHeight() {
        viewModelScope.launch {
            settingsRepository.updateLayoutSettings {
                copy(spacerHeight = LayoutSettings().spacerHeight)
            }
        }
    }
}
