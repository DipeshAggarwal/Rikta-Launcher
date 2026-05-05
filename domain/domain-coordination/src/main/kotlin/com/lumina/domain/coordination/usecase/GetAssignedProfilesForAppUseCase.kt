package com.lumina.domain.coordination.usecase

import com.lumina.domain.profiles.ProfileRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetAssignedProfilesForAppUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(packageName: String, userHandleNumber: Long): Flow<Set<String>> {
        return profileRepository.getProfileIdsForApp(packageName, userHandleNumber)
    }
}
