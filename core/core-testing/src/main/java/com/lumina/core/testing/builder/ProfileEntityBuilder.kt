package com.lumina.core.testing.builder

import com.lumina.core.database.entity.ProfileAuth
import com.lumina.core.database.entity.ProfileEntity
import com.lumina.core.database.entity.ProfileOverrides
import com.lumina.core.database.entity.ProfilePermissions
import com.lumina.core.database.entity.ProfileRestrictions
import com.lumina.core.database.entity.ProfileSettings
import com.lumina.core.model.AppCategory
import com.lumina.core.model.AppUsageEnforcementMode
import com.lumina.core.model.ProfileAuthMethod
import com.lumina.core.model.ProfileBackground
import com.lumina.core.model.ProfileType
import com.lumina.domain.profiles.model.LauncherProfileOverrides
import com.lumina.domain.profiles.model.LauncherProfileSettings

object ProfileEntityBuilder {
    fun build(
        id: String = "profile_test",
        name: String = "Test Profile",
        type: ProfileType = ProfileType.CUSTOM,
        description: String = "",
        createdAt: Long = 0L,
        updatedAt: Long = 0L,
        userHandleNumber: Long = 0L,
        isAdmin: Boolean = false,
        priorityTriggerLaunch: Boolean = false,
        startDnd: Boolean = false,
        hideScreenTimeOnApps: Boolean = false,
        maxAppCount: Int = 0,
        autoAddCategoryApps: List<AppCategory> = emptyList(),
        allowLauncherSettingsChange: Boolean = false,
        allowManagingApps: Boolean = false,
        allowLauncherAppActions: Boolean = false,
        allowProfileManagement: Boolean = false,
        appUsageEnforcementMode: AppUsageEnforcementMode = AppUsageEnforcementMode.COUNTDOWN,
        blockAppList: Boolean = false,
        blockProfileTriggerSwitching: Boolean = false,
        blockUnauthorisedApps: Boolean = false,
        blockRecentApps: Boolean = false,
        blockSystemAppAdd: Boolean = false,
        blockNotificationShade: Boolean = false,
        filterNotifications: Boolean = false,
        switchOnDeviceLock: Boolean = false,
        entryAuthMethod: ProfileAuthMethod = ProfileAuthMethod.NONE,
        exitAuthMethod: ProfileAuthMethod = ProfileAuthMethod.NONE,
        activationKey: String? = null,
        theme: String? = null,
        background: ProfileBackground? = null,
        font: String? = null,
        showClock: Boolean? = null,
        showBigClock: Boolean? = null,
        showDate: Boolean? = null,
        showWeather: Boolean? = null,
        hideScreenTime: Boolean? = null,
        iconName: String? = null,
    ) = ProfileEntity(
        id = id,
        userHandleNumber = userHandleNumber,
        type = type,
        name = name,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isAdmin = isAdmin,
        priorityTriggerLaunch = priorityTriggerLaunch,
        settings = ProfileSettings(
            startDnd = startDnd,
            hideScreenTimeOnApps = hideScreenTimeOnApps,
            maxAppCount = maxAppCount,
            autoAddCategoryApps = autoAddCategoryApps
        ),
        permissions = ProfilePermissions(
            allowLauncherSettingsChange = allowLauncherSettingsChange,
            allowManagingApps = allowManagingApps,
            allowLauncherAppActions = allowLauncherAppActions,
            allowProfileManagement = allowProfileManagement
        ),
        restrictions = ProfileRestrictions(
            appUsageEnforcementMode = appUsageEnforcementMode,
            blockAppList = blockAppList,
            blockProfileTriggerSwitching = blockProfileTriggerSwitching,
            blockUnauthorisedApps = blockUnauthorisedApps,
            blockRecentApps = blockRecentApps,
            blockSystemAppAdd = blockSystemAppAdd,
            blockNotificationShade = blockNotificationShade,
            filterNotifications = filterNotifications,
            switchOnDeviceLock = switchOnDeviceLock
        ),
        auth = ProfileAuth(
            entryAuthMethod = entryAuthMethod,
            exitAuthMethod = exitAuthMethod,
            activationKey = activationKey
        ),
        overrides = ProfileOverrides(
            theme = theme,
            background = background,
            font = font,
            showClock = showClock,
            showBigClock = showBigClock,
            showDate = showDate,
            showWeather = showWeather,
            hideScreenTime = hideScreenTime,
            iconName = iconName
        )
    )
}
