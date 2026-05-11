package com.lumina.domain.coordination.usecase

import com.lumina.core.common.time.TimeProvider
import com.lumina.core.model.AppUsageEnforcementMode
import com.lumina.core.model.ProfileAuthMethod
import com.lumina.core.model.ProfileType
import com.lumina.domain.coordination.DeviceUserProvider
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.domain.profiles.model.LauncherProfileAuth
import com.lumina.domain.profiles.model.LauncherProfileOverrides
import com.lumina.domain.profiles.model.LauncherProfilePermissions
import com.lumina.domain.profiles.model.LauncherProfileRestrictions
import com.lumina.domain.profiles.model.LauncherProfileSettings
import jakarta.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CreateDraftProfileUseCase @Inject constructor(
    private val deviceUserProvider: DeviceUserProvider,
    private val timeProvider: TimeProvider
){
    @OptIn(ExperimentalUuidApi::class)
    operator fun invoke(): LauncherProfile {
        val now = timeProvider.now()

        return LauncherProfile(
            id = Uuid.random().toString(),
            userHandleNumber = deviceUserProvider.getCurrentUserSerialNumber(),
            type = ProfileType.CUSTOM,
            name = "",
            description = null,

            createdAt = now,
            updatedAt = now,

            isAdmin = false,
            priorityTriggerLaunch = false,

            settings = LauncherProfileSettings(
                startDnd = false,
                hideScreenTimeOnApps = false,
                maxAppCount = 0,
                autoAddCategoryApps = emptyList()
            ),
            permissions = LauncherProfilePermissions(
                allowLauncherSettingsChange = false,
                allowManagingApps = false,
                allowLauncherAppActions = false,
                allowProfileManagement = false
            ),
            restrictions = LauncherProfileRestrictions(
                appUsageEnforcementMode = AppUsageEnforcementMode.REMINDER,
                blockAppList = false,
                blockProfileTriggerSwitching = false,
                blockUnauthorisedApps = false,
                blockRecentApps = false,
                blockSystemAppAdd = false,
                blockNotificationShade = false,
                filterNotifications = false,
                switchOnDeviceLock = false
            ),
            auth = LauncherProfileAuth(
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
                iconName = null
            )
        )
    }
}
