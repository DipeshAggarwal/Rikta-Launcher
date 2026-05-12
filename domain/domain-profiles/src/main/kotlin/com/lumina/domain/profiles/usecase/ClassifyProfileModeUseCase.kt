package com.lumina.domain.profiles.usecase

import com.lumina.core.model.AppUsageEnforcementMode
import com.lumina.core.model.ProfileAuthMethod
import com.lumina.core.model.ProfileClassification
import com.lumina.core.model.ProfilePreset
import com.lumina.domain.profiles.PresetDefaults
import com.lumina.domain.profiles.model.LauncherProfile
import jakarta.inject.Inject

class ClassifyProfileModeUseCase @Inject constructor() {

    operator fun invoke(profile: LauncherProfile): ProfileClassification {
        val preset = PresetDefaults.presets
            .firstOrNull { it.isCanonical(profile) }
            ?: PresetDefaults.Custom

        return ProfileClassification(
            preset = preset.preset,
            isCustomised = !preset.exactMatch(profile)
        )
    }
}
