package com.lumina.domain.coordination.usecase

import com.lumina.core.model.AppBasicData
import com.lumina.core.model.ProfileAuthMethod
import com.lumina.core.model.ProfileType
import com.lumina.core.model.SystemProfileIds
import com.lumina.domain.apps.InstalledAppsRepository
import com.lumina.domain.coordination.DeviceUserProvider
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.LauncherProfile
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first

class InitialiseDefaultProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val deviceUserProvider: DeviceUserProvider,
    private val installedAppsRepository: InstalledAppsRepository
) {
    suspend operator fun invoke() {
        val defaultProfile = profileRepository.getProfileById(SystemProfileIds.DEFAULT).first()
        if (defaultProfile == null) {
            val defaultUserHandle = deviceUserProvider.getCurrentUserSerialNumber()
            val newDefaultProfile = LauncherProfile(
                id = SystemProfileIds.DEFAULT,
                userHandleNumber = defaultUserHandle,
                type = ProfileType.CUSTOM,
                name = "Default",
                strictMode = false,
                priorityTriggerLaunch = false,
                filterNotification = false,
                blockProfileTriggerSwitching = false,
                startDnd = false,
                showAppList = true,
                hideScreenTimeOnApps = false,
                disableOnLock = false,
                blockUnauthorisedApps = false,
                overrideBackground = null,
                overrideFont = null,
                overrideShowClock = false,
                overrideShowBigClock = false,
                overrideShowDate = false,
                overrideShowWeather = false,
                overrideHideScreenTime = false,
                entryAuthMethod = ProfileAuthMethod.NONE,
                exitAuthMethod = ProfileAuthMethod.NONE,
                activationKey = null
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
        }
    }
}
