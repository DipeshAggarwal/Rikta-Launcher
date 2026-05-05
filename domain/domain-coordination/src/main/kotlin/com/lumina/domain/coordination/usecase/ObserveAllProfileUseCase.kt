package com.lumina.domain.coordination.usecase

import com.lumina.domain.profiles.ProfileRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveAllProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(): Flow<List<Pair<String, String>>> {
        return profileRepository.getAllProfiles().map { profiles ->
            profiles.map { profile -> profile.id to profile.name }
        }
    }
}
