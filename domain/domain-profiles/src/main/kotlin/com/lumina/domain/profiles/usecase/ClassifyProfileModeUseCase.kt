package com.lumina.domain.profiles.usecase

import com.lumina.core.model.AppUsageEnforcementMode
import com.lumina.core.model.ProfileAuthMethod
import com.lumina.core.model.ProfileClassification
import com.lumina.core.model.ProfileMode
import com.lumina.domain.profiles.model.LauncherProfile
import jakarta.inject.Inject

class ClassifyProfileModeUseCase @Inject constructor() {

    operator fun invoke(profile: LauncherProfile): ProfileClassification {
        val mode = classify(profile)
        val isCustomised = !isCanonical(mode, profile)

        return ProfileClassification(mode = mode, isCustomised = isCustomised)
    }

    private fun classify(profile: LauncherProfile): ProfileMode = when {
        isAdmin(profile) -> ProfileMode.ADMIN
        isPrivate(profile) -> ProfileMode.PRIVATE
        isWork(profile) -> ProfileMode.WORK
        isFocus(profile) -> ProfileMode.FOCUS
        isGuest(profile) -> ProfileMode.GUEST
        isKid(profile) -> ProfileMode.KID
        else -> ProfileMode.CUSTOM
    }

    private fun isCanonical(mode: ProfileMode, profile: LauncherProfile): Boolean {
        val permissions = profile.permissions
        val restrictions = profile.restrictions
        val settings = profile.settings
        val auth = profile.auth

        return when (mode) {
            ProfileMode.ADMIN -> profile.isAdmin
                    && permissions.allowLauncherAppActions
                    && permissions.allowLauncherSettingsChange
                    && permissions.allowProfileManagement
                    && permissions.allowManagingApps

            ProfileMode.PRIVATE -> !profile.isAdmin
                    && restrictions.switchOnDeviceLock
                    && auth.entryAuthMethod != ProfileAuthMethod.NONE

            ProfileMode.WORK -> profile.priorityTriggerLaunch
                    && restrictions.filterNotifications
                    && settings.startDnd
                    && restrictions.blockUnauthorisedApps

            ProfileMode.FOCUS -> restrictions.appUsageEnforcementMode == AppUsageEnforcementMode.HARD_BLOCK
                    && restrictions.filterNotifications
                    && restrictions.blockAppList
                    && restrictions.blockRecentApps
                    && restrictions.blockUnauthorisedApps
                    && restrictions.blockProfileTriggerSwitching
                    && settings.startDnd

            ProfileMode.GUEST -> !permissions.allowProfileManagement
                    && !permissions.allowLauncherAppActions
                    && !permissions.allowLauncherSettingsChange
                    && !permissions.allowManagingApps
                    && restrictions.blockAppList
                    && restrictions.blockUnauthorisedApps
                    && restrictions.blockProfileTriggerSwitching
                    && restrictions.filterNotifications
                    && restrictions.blockSystemAppAdd
                    && auth.exitAuthMethod != ProfileAuthMethod.NONE

            ProfileMode.KID -> !permissions.allowProfileManagement
                    && !permissions.allowLauncherAppActions
                    && !permissions.allowLauncherSettingsChange
                    && !permissions.allowManagingApps
                    && restrictions.appUsageEnforcementMode == AppUsageEnforcementMode.HARD_BLOCK
                    && restrictions.blockAppList
                    && restrictions.blockUnauthorisedApps
                    && restrictions.blockProfileTriggerSwitching
                    && restrictions.filterNotifications
                    && restrictions.blockSystemAppAdd

            ProfileMode.CUSTOM -> true
        }
    }

    //--------------------------------------------------
    // Classifiers

    private fun isAdmin(profile: LauncherProfile): Boolean = profile.isAdmin

    private fun isPrivate(profile: LauncherProfile): Boolean =
        !profile.isAdmin &&
        profile.restrictions.switchOnDeviceLock &&
        profile.auth.entryAuthMethod != ProfileAuthMethod.NONE

    private fun isWork(profile: LauncherProfile): Boolean {
        val score = listOf(
            profile.priorityTriggerLaunch,
            profile.restrictions.filterNotifications,
            profile.settings.startDnd,
            profile.restrictions.blockUnauthorisedApps
        ).count { it }
        return score >= 2
    }

    private fun isFocus(profile: LauncherProfile): Boolean {
        val score = listOf(
            profile.restrictions.appUsageEnforcementMode == AppUsageEnforcementMode.HARD_BLOCK,
            profile.restrictions.filterNotifications,
            profile.restrictions.blockAppList,
            profile.restrictions.blockRecentApps,
            profile.restrictions.blockUnauthorisedApps,
            profile.restrictions.blockProfileTriggerSwitching,
            profile.settings.startDnd
        ).count { it }
        return score >= 4
    }

    private fun isGuest(profile: LauncherProfile): Boolean {
        val score = listOf(
            !profile.permissions.allowProfileManagement,
            !profile.permissions.allowLauncherAppActions,
            !profile.permissions.allowLauncherSettingsChange,
            !profile.permissions.allowManagingApps,
            profile.restrictions.blockAppList,
            profile.restrictions.blockUnauthorisedApps,
            profile.restrictions.blockProfileTriggerSwitching,
            profile.restrictions.filterNotifications,
            profile.restrictions.blockSystemAppAdd,
            profile.auth.exitAuthMethod != ProfileAuthMethod.NONE
        ).count { it }
        return score >= 5
    }

    private fun isKid(profile: LauncherProfile): Boolean {
        val score = listOf(
            !profile.permissions.allowProfileManagement,
            !profile.permissions.allowLauncherAppActions,
            !profile.permissions.allowLauncherSettingsChange,
            !profile.permissions.allowManagingApps,
            profile.restrictions.appUsageEnforcementMode == AppUsageEnforcementMode.HARD_BLOCK,
            profile.restrictions.blockAppList,
            profile.restrictions.blockUnauthorisedApps,
            profile.restrictions.blockProfileTriggerSwitching,
            profile.restrictions.filterNotifications,
            profile.restrictions.blockSystemAppAdd
        ).count { it }
        return score >= 4
    }
}
