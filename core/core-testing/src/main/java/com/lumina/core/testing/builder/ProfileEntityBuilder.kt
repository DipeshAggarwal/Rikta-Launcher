package com.lumina.core.testing.builder

import com.lumina.core.database.entity.ProfileEntity
import com.lumina.core.model.ProfileAuthMethod
import com.lumina.core.model.ProfileType

object ProfileEntityBuilder {
    fun build(
        id: String = "profile_test",
        name: String = "Test Profile",
        type: ProfileType = ProfileType.CUSTOM,
        userHandleNumber: Long = 0L,
        blockProfileTriggerSwitching: Boolean = false,
        strictMode: Boolean = false,
        priorityTriggerLaunch: Boolean = false,
        filterNotification: Boolean = false,
        startDnd: Boolean = false,
        showAppList: Boolean = true,
        hideScreenTimeOnApps: Boolean = false,
        disableOnLock: Boolean = false,
        blockUnauthorisedApps: Boolean = false,
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
    ) = ProfileEntity(
        id = id,
        userHandleNumber = userHandleNumber,
        type = type,
        name = name,
        strictMode = strictMode,
        priorityTriggerLaunch = priorityTriggerLaunch,
        filterNotification = filterNotification,
        blockProfileTriggerSwitching = blockProfileTriggerSwitching,
        startDnd = startDnd,
        showAppList = showAppList,
        hideScreenTimeOnApps = hideScreenTimeOnApps,
        disableOnLock = disableOnLock,
        blockUnauthorisedApps = blockUnauthorisedApps,
        overrideBackground = overrideBackground,
        overrideFont = overrideFont,
        overrideShowClock = overrideShowClock,
        overrideShowBigClock = overrideShowBigClock,
        overrideShowDate = overrideShowDate,
        overrideShowWeather = overrideShowWeather,
        overrideHideScreenTime = overrideHideScreenTime,
        entryAuthMethod = entryAuthMethod,
        exitAuthMethod = exitAuthMethod,
        activationKey = activationKey,
    )
}
