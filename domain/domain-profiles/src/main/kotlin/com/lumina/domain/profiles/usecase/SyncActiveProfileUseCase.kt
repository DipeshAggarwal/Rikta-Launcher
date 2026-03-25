package com.lumina.domain.profiles.usecase

import com.lumina.core.model.SystemProfileIds
import com.lumina.domain.profiles.ProfileRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first

class SyncActiveProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke() {
        if (profileRepository.activeProfile.first() == null) {
            profileRepository.setActiveProfile(SystemProfileIds.DEFAULT)
        }
    }
}
