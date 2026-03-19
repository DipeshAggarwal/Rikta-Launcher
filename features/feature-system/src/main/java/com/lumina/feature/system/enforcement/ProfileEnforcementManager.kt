package com.lumina.feature.system.enforcement

import com.lumina.core.logging.Logger
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.feature.system.LuminaDndController
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Singleton
class ProfileEnforcementManager @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val dndController: LuminaDndController,
    private val logger: Logger
) {
    private val TAG = this::class.java.simpleName

    fun start(scope: CoroutineScope) {
        profileRepository.activeProfile
            .onEach { profile ->
                if (profile == null) return@onEach

                logger.d(TAG, "Enforcing system settings for profile: ${profile.name}.")
                if (profile.startDnd) {
                    dndController.setDndEnabled(true)
                } else {
                    dndController.setDndEnabled(false)
            }
            }
            .launchIn(scope)
    }
}
