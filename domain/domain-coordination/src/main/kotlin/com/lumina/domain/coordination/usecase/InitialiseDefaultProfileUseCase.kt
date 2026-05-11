package com.lumina.domain.coordination.usecase

import com.lumina.core.common.time.TimeProvider
import com.lumina.core.model.AppBasicData
import com.lumina.core.model.AppUsageEnforcementMode
import com.lumina.core.model.ProfileType
import com.lumina.core.model.SystemProfileIds
import com.lumina.domain.apps.InstalledAppsRepository
import com.lumina.domain.coordination.DeviceUserProvider
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.LauncherProfilePermissions
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first

class InitialiseDefaultProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val deviceUserProvider: DeviceUserProvider,
    private val installedAppsRepository: InstalledAppsRepository,
    private val timeProvider: TimeProvider,
    private val createDraftProfileUseCase: CreateDraftProfileUseCase
) {
    suspend operator fun invoke() {
        val defaultProfile = profileRepository.getProfileById(SystemProfileIds.DEFAULT).first()
        val defaultUserHandle = deviceUserProvider.getCurrentUserSerialNumber()

        if (defaultProfile == null) {
            val newDefaultProfile = createDraftProfileUseCase().copy(
                id = SystemProfileIds.DEFAULT,
                userHandleNumber = defaultUserHandle,
                type = ProfileType.LAUNCHER_DEFAULT,
                name = SystemProfileIds.DEFAULT,
                createdAt = timeProvider.now(),
                updatedAt = timeProvider.now(),
                isAdmin = true,
                priorityTriggerLaunch = false,
                permissions = LauncherProfilePermissions(
                    allowLauncherSettingsChange = true,
                    allowManagingApps = true,
                    allowLauncherAppActions = true,
                    allowProfileManagement = true
                )
            )

            profileRepository.saveProfile(newDefaultProfile)
            val allApps = installedAppsRepository.apps.first { it.isNotEmpty() }
            val defaultUserApps = allApps
                .filter { it.userHandleNumber == defaultUserHandle }
                .map { AppBasicData(it.packageName, it.userHandleNumber) }

            if (defaultUserApps.isNotEmpty()) {
                profileRepository.addAppsToProfile(SystemProfileIds.DEFAULT, defaultUserApps)
            }

            profileRepository.setActiveProfile(SystemProfileIds.DEFAULT)
            createSecondaryProfiles()
        }
    }

    private suspend fun createSecondaryProfiles() {
        val defaultUserHandle = deviceUserProvider.getCurrentUserSerialNumber()
        val existingIds = profileRepository.getAllProfiles().first().map { it.id }.toSet()

        SystemProfileIds.AUTO_CREATE.forEach { id ->
            if (id in existingIds) return@forEach

            val draftProfile = createDraftProfileUseCase()
            val restrictions = when (id) {
                SystemProfileIds.WORK -> draftProfile.restrictions.copy(
                    filterNotifications = true,
                    blockUnauthorisedApps = true
                )
                SystemProfileIds.GUEST -> draftProfile.restrictions.copy(
                    blockProfileTriggerSwitching = true,
                    blockUnauthorisedApps = true,
                    blockSystemAppAdd = true,
                    filterNotifications = true
                )
                SystemProfileIds.FOCUS -> draftProfile.restrictions.copy(
                    appUsageEnforcementMode = AppUsageEnforcementMode.HARD_BLOCK,
                    blockAppList = true,
                    blockProfileTriggerSwitching = true,
                    blockUnauthorisedApps = true,
                    blockRecentApps = true,
                    blockSystemAppAdd = true,
                    blockNotificationShade = true,
                    filterNotifications = true
                )
                else -> draftProfile.restrictions
            }
            val profile = draftProfile.copy(
                id = id,
                userHandleNumber = defaultUserHandle,
                type = if (id == SystemProfileIds.GUEST) ProfileType.LAUNCHER_GUEST
                    else ProfileType.LAUNCHER_DEFAULT,
                name = id,
                restrictions = restrictions
            )
            profileRepository.saveProfile(profile)
        }
    }
}
