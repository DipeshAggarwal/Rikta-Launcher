package com.lumina.domain.coordination.usecase

import com.lumina.core.model.AppBasicData
import com.lumina.core.model.ProfileAuthMethod
import com.lumina.core.model.ProfileType
import com.lumina.core.model.SystemProfileIds
import com.lumina.domain.apps.InstalledAppsRepository
import com.lumina.domain.coordination.DeviceUserProvider
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.domain.profiles.model.LauncherProfileOverrides
import com.lumina.domain.profiles.model.LauncherProfileSettings
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first

class InitialiseDefaultProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val deviceUserProvider: DeviceUserProvider,
    private val installedAppsRepository: InstalledAppsRepository
) {
    suspend operator fun invoke() {
        val defaultProfile = profileRepository.getProfileById(SystemProfileIds.DEFAULT).first()
        val defaultUserHandle = deviceUserProvider.getCurrentUserSerialNumber()

        if (defaultProfile == null) {
            val newDefaultProfile = LauncherProfile(
                id = SystemProfileIds.DEFAULT,
                userHandleNumber = defaultUserHandle,
                type = ProfileType.CUSTOM,
                name = "Default",
                settings = LauncherProfileSettings(
                    strictMode = false,
                    isAdmin = true,
                    priorityTriggerLaunch = false,
                    filterNotification = false,
                    blockProfileTriggerSwitching = false,
                    allowAppRename = true,
                    allowProfileManagement = true,
                    allowAppCategoryChange = true,
                    startDnd = false,
                    showAppList = true,
                    hideScreenTimeOnApps = false,
                    disableOnLock = false,
                    blockUnauthorisedApps = false,
                    entryAuthMethod = ProfileAuthMethod.NONE,
                    exitAuthMethod = ProfileAuthMethod.NONE,
                    activationKey = null
                ),
                overrides = LauncherProfileOverrides(
                    theme = null,
                    background = null,
                    font = null,
                    showClock = true,
                    showBigClock = true,
                    showDate = true,
                    showWeather = true,
                    hideScreenTime = false,
                ),
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
            setSecondaryProfiles()
        }
    }

    private suspend fun setSecondaryProfiles() {
        val defaultUserHandle = deviceUserProvider.getCurrentUserSerialNumber()
        val defaultSettings = LauncherProfileSettings(
            strictMode = false,
            isAdmin = false,
            priorityTriggerLaunch = false,
            filterNotification = true,
            blockProfileTriggerSwitching = false,
            allowAppRename = false,
            allowProfileManagement = false,
            allowAppCategoryChange = false,
            startDnd = false,
            showAppList = true,
            hideScreenTimeOnApps = false,
            disableOnLock = false,
            blockUnauthorisedApps = false,
            entryAuthMethod = ProfileAuthMethod.NONE,
            exitAuthMethod = ProfileAuthMethod.NONE,
            activationKey = null
        )
        val defaultOverrides = LauncherProfileOverrides(
            theme = null,
            background = null,
            font = null,
            showClock = true,
            showBigClock = true,
            showDate = true,
            showWeather = true,
            hideScreenTime = false,
        )

        val existingIds = profileRepository.getAllProfiles().first().map { it.id }.toSet()
        SystemProfileIds.AUTO_CREATE.forEach { id ->
            if (id !in existingIds) {
                val settings = when (id) {
                    SystemProfileIds.GUEST -> defaultSettings.copy(
                        strictMode = true,
                        blockProfileTriggerSwitching = true,
                        blockUnauthorisedApps = true,
                    )
                    SystemProfileIds.FOCUS -> defaultSettings.copy(
                        strictMode = true,
                        blockProfileTriggerSwitching = true,
                        showAppList = false,
                        blockUnauthorisedApps = true,
                    )
                    else -> defaultSettings
                }
                val profile = buildProfile(
                    id = id,
                    userHandleNumber = defaultUserHandle,
                    type = if (id == SystemProfileIds.GUEST) ProfileType.LAUNCHER_GUEST
                    else ProfileType.LAUNCHER_DEFAULT,
                    name = id,
                    settings = settings,
                    overrides = defaultOverrides
                )
                profileRepository.saveProfile(profile)
            }

        }
    }

    private fun buildProfile(
        id: String,
        userHandleNumber: Long,
        type: ProfileType,
        name: String,
        settings: LauncherProfileSettings,
        overrides: LauncherProfileOverrides
    ) = LauncherProfile(
        id = id,
        userHandleNumber = userHandleNumber,
        type = type,
        name = name,
        settings = settings,
        overrides = overrides
    )
}
