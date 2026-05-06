package com.lumina.core.testing.builder

import com.lumina.core.model.ProfileAuthMethod
import com.lumina.core.model.ProfileType
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.domain.profiles.model.LauncherProfileOverrides
import com.lumina.domain.profiles.model.LauncherProfileSettings
import kotlin.Boolean

object LauncherProfileBuilder {
    fun build(
        id: String = "profile_test",
        name: String = "Test Profile",
        type: ProfileType = ProfileType.CUSTOM,
        userHandleNumber: Long = 0L,
        strictMode: Boolean = false,
        isAdmin: Boolean = false,
        blockProfileTriggerSwitching: Boolean = false,
        priorityTriggerLaunch: Boolean = false,
        filterNotification: Boolean = false,
        allowAppRename: Boolean = false,
        allowProfileManagement: Boolean = false,
        allowAppCategoryChange: Boolean = false,
        startDnd: Boolean = false,
        showAppList: Boolean = true,
        hideScreenTimeOnApps: Boolean = false,
        disableOnLock: Boolean = false,
        blockUnauthorisedApps: Boolean = false,
        overrideTheme: String? = null,
        overrideBackground: String? = null,
        overrideFont: String? = null,
        overrideShowClock: Boolean? = null,
        overrideShowBigClock: Boolean? = null,
        overrideShowDate: Boolean? = null,
        overrideShowWeather: Boolean? = null,
        overrideHideScreenTime: Boolean? = null,
        entryAuthMethod: ProfileAuthMethod = ProfileAuthMethod.NONE,
        exitAuthMethod: ProfileAuthMethod = ProfileAuthMethod.NONE,
        activationKey: String? = null,
    ) = LauncherProfile(
        id = id,
        name = name,
        type = type,
        userHandleNumber = userHandleNumber,
        settings = LauncherProfileSettings(
            strictMode = strictMode,
            isAdmin = isAdmin,
            blockProfileTriggerSwitching = blockProfileTriggerSwitching,
            priorityTriggerLaunch = priorityTriggerLaunch,
            filterNotification = filterNotification,
            allowAppRename = allowAppRename,
            allowProfileManagement = allowProfileManagement,
            allowAppCategoryChange = allowAppCategoryChange,
            startDnd = startDnd,
            showAppList = showAppList,
            hideScreenTimeOnApps = hideScreenTimeOnApps,
            disableOnLock = disableOnLock,
            blockUnauthorisedApps = blockUnauthorisedApps,
            entryAuthMethod = entryAuthMethod,
            exitAuthMethod = exitAuthMethod,
            activationKey = activationKey
        ),
        overrides = LauncherProfileOverrides(
            theme = overrideTheme,
            background = overrideBackground,
            font = overrideFont,
            showClock = overrideShowClock,
            showBigClock = overrideShowBigClock,
            showDate = overrideShowDate,
            showWeather = overrideShowWeather,
            hideScreenTime = overrideHideScreenTime
        )
    )
}
