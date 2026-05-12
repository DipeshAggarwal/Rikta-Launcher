package com.lumina.domain.profiles

import com.lumina.core.model.AppUsageEnforcementMode
import com.lumina.core.model.ProfileAuthMethod
import com.lumina.core.model.ProfilePreset
import com.lumina.domain.profiles.model.LauncherProfile

sealed class PresetDefaults {
    abstract val preset: ProfilePreset
    abstract fun applyTo(profile: LauncherProfile): LauncherProfile
    abstract fun exactMatch(profile: LauncherProfile): Boolean
    abstract fun isCanonical(profile: LauncherProfile): Boolean
    data object Admin : PresetDefaults() {
        override val preset: ProfilePreset = ProfilePreset.ADMIN

        override fun applyTo(profile: LauncherProfile) = profile.copy(
            permissions = profile.permissions.copy(
                allowLauncherAppActions = true,
                allowLauncherSettingsChange = true,
                allowProfileManagement = true,
                allowManagingApps = true
            )
        )

        override fun exactMatch(profile: LauncherProfile) = profile.isAdmin
                && profile.permissions.allowLauncherAppActions
                && profile.permissions.allowLauncherSettingsChange
                && profile.permissions.allowProfileManagement
                && profile.permissions.allowManagingApps

        override fun isCanonical(profile: LauncherProfile): Boolean = profile.isAdmin
    }

    data object Private : PresetDefaults() {
        override val preset: ProfilePreset = ProfilePreset.PRIVATE

        override fun applyTo(profile: LauncherProfile) = profile.copy(
            restrictions = profile.restrictions.copy(
                switchOnDeviceLock = true
            ),
            auth = profile.auth.copy(
                entryAuthMethod = ProfileAuthMethod.PIN
            )
        )

        override fun exactMatch(profile: LauncherProfile) = !profile.isAdmin
                && profile.restrictions.switchOnDeviceLock
                && profile.auth.entryAuthMethod != ProfileAuthMethod.NONE

        override fun isCanonical(profile: LauncherProfile): Boolean = !profile.isAdmin &&
                profile.restrictions.switchOnDeviceLock &&
                profile.auth.entryAuthMethod != ProfileAuthMethod.NONE
    }

    data object Work : PresetDefaults() {
        override val preset: ProfilePreset = ProfilePreset.WORK

        override fun applyTo(profile: LauncherProfile) = profile.copy(
            restrictions = profile.restrictions.copy(
                filterNotifications = true,
                blockUnauthorisedApps = true
            ),
            settings = profile.settings.copy(
                startDnd = true
            )
        )

        override fun exactMatch(profile: LauncherProfile) = profile.priorityTriggerLaunch
                && profile.restrictions.filterNotifications
                && profile.settings.startDnd
                && profile.restrictions.blockUnauthorisedApps

        override fun isCanonical(profile: LauncherProfile): Boolean = listOf(
            profile.priorityTriggerLaunch,
            profile.restrictions.filterNotifications,
            profile.settings.startDnd,
            profile.restrictions.blockUnauthorisedApps
        ).count { it } >= 2
    }

    data object Focus : PresetDefaults() {
        override val preset: ProfilePreset = ProfilePreset.FOCUS

        override fun applyTo(profile: LauncherProfile) = profile.copy(
            restrictions = profile.restrictions.copy(
                appUsageEnforcementMode = AppUsageEnforcementMode.HARD_BLOCK,
                filterNotifications = true,
                blockAppList = true,
                blockRecentApps = true,
                blockUnauthorisedApps = true,
                blockProfileTriggerSwitching = true
            ),
            settings = profile.settings.copy(
                startDnd = true
            )
        )

        override fun exactMatch(profile: LauncherProfile) =
            profile.restrictions.appUsageEnforcementMode == AppUsageEnforcementMode.HARD_BLOCK
                    && profile.restrictions.filterNotifications
                    && profile.restrictions.blockAppList
                    && profile.restrictions.blockRecentApps
                    && profile.restrictions.blockUnauthorisedApps
                    && profile.restrictions.blockProfileTriggerSwitching
                    && profile.settings.startDnd

        override fun isCanonical(profile: LauncherProfile): Boolean = listOf(
            profile.restrictions.appUsageEnforcementMode == AppUsageEnforcementMode.HARD_BLOCK,
            profile.restrictions.filterNotifications,
            profile.restrictions.blockAppList,
            profile.restrictions.blockRecentApps,
            profile.restrictions.blockUnauthorisedApps,
            profile.restrictions.blockProfileTriggerSwitching,
            profile.settings.startDnd
        ).count { it } >= 4
    }

    data object Guest : PresetDefaults() {
        override val preset: ProfilePreset = ProfilePreset.GUEST

        override fun applyTo(profile: LauncherProfile) = profile.copy(
            permissions = profile.permissions.copy(
                allowProfileManagement = false,
                allowLauncherAppActions = false,
                allowLauncherSettingsChange = false,
                allowManagingApps = false
            ),
            restrictions = profile.restrictions.copy(
                blockAppList = true,
                blockUnauthorisedApps = true,
                blockProfileTriggerSwitching = true,
                filterNotifications = true,
                blockSystemAppAdd = true
            ),
            auth = profile.auth.copy(
                exitAuthMethod = ProfileAuthMethod.PIN
            )
        )

        override fun exactMatch(profile: LauncherProfile) = !profile.permissions.allowProfileManagement
                && !profile.permissions.allowLauncherAppActions
                && !profile.permissions.allowLauncherSettingsChange
                && !profile.permissions.allowManagingApps
                && profile.restrictions.blockAppList
                && profile.restrictions.blockUnauthorisedApps
                && profile.restrictions.blockProfileTriggerSwitching
                && profile.restrictions.filterNotifications
                && profile.restrictions.blockSystemAppAdd
                && profile.auth.exitAuthMethod != ProfileAuthMethod.NONE

        override fun isCanonical(profile: LauncherProfile): Boolean = listOf(
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
        ).count { it } >= 5
    }

    data object Kid : PresetDefaults() {
        override val preset: ProfilePreset = ProfilePreset.KID

        override fun applyTo(profile: LauncherProfile) = profile.copy(
            permissions = profile.permissions.copy(
                allowProfileManagement = false,
                allowLauncherAppActions = false,
                allowLauncherSettingsChange = false,
                allowManagingApps = false
            ),
            restrictions = profile.restrictions.copy(
                appUsageEnforcementMode = AppUsageEnforcementMode.HARD_BLOCK,
                blockAppList = true,
                blockUnauthorisedApps = true,
                blockProfileTriggerSwitching = true,
                filterNotifications = true,
                blockSystemAppAdd = true
            )
        )

        override fun exactMatch(profile: LauncherProfile) = !profile.permissions.allowProfileManagement
                && !profile.permissions.allowLauncherAppActions
                && !profile.permissions.allowLauncherSettingsChange
                && !profile.permissions.allowManagingApps
                && profile.restrictions.appUsageEnforcementMode == AppUsageEnforcementMode.HARD_BLOCK
                && profile.restrictions.blockAppList
                && profile.restrictions.blockUnauthorisedApps
                && profile.restrictions.blockProfileTriggerSwitching
                && profile.restrictions.filterNotifications
                && profile.restrictions.blockSystemAppAdd

        override fun isCanonical(profile: LauncherProfile): Boolean = listOf(
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
        ).count { it } >= 5
    }

    data object Custom : PresetDefaults() {
        override val preset: ProfilePreset = ProfilePreset.CUSTOM

        override fun applyTo(profile: LauncherProfile) = profile
        override fun exactMatch(profile: LauncherProfile) = true

        override fun isCanonical(profile: LauncherProfile): Boolean = true
    }

    companion object {
        val presets = listOf(
            Admin,
            Private,
            Work,
            Focus,
            Guest,
            Kid
        )

        fun forPreset(preset: ProfilePreset): PresetDefaults = when (preset) {
            ProfilePreset.ADMIN -> Admin
            ProfilePreset.PRIVATE -> Private
            ProfilePreset.WORK -> Work
            ProfilePreset.FOCUS -> Focus
            ProfilePreset.GUEST -> Guest
            ProfilePreset.KID -> Kid
            ProfilePreset.CUSTOM -> Custom
        }
    }
}
