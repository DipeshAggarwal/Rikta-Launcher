package com.lumina.domain.profiles

import com.lumina.core.model.AppBasicData
import com.lumina.domain.profiles.model.AppOverrideState
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.domain.profiles.model.TriggerCondition
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    val activeProfile: Flow<LauncherProfile?>
    suspend fun setActiveProfile(profileId: String)
    suspend fun clearActiveProfile()

    fun getAllProfiles(): Flow<List<LauncherProfile>>
    fun getProfileById(profileId: String): Flow<LauncherProfile?>

    suspend fun isActivationKeyUnique(key: String, excludeProfileId: String): Boolean
    suspend fun findProfileByKey(key: String): LauncherProfile?

    suspend fun saveProfile(profile: LauncherProfile)
    suspend fun updateProfile(profile: LauncherProfile)
    suspend fun deleteProfile(profileId: String)

    fun getAppsForProfile(profileId: String): Flow<List<AppOverrideState>>
    suspend fun addAppToProfile(profileId: String, packageName: String, userHandleNumber: Long)
    suspend fun removeAppFromProfile(profileId: String, packageName: String, userHandleNumber: Long)
    suspend fun updateAppOverride(
        profileId: String,
        packageName: String,
        userHandleNumber: Long,
        customName: String?,
        recommendedUsageMinutes: Int?,
        customCountdown: Int?
    )

    fun getProfileTriggers(profileId: String): Flow<List<TriggerCondition>>
    suspend fun addProfileTrigger(profileId: String, conditions: TriggerCondition)
    suspend fun removeProfileTrigger(triggerId: Long)
    suspend fun clearAllProfileTriggers(profileId: String)

    fun getNotificationWhitelist(profileId: String): Flow<Set<AppBasicData>>
    fun getNotificationAllowedApps(profileId: String): Flow<Set<AppBasicData>>
    suspend fun saveNotificationWhitelist(profileId: String, packageName: String, userHandleNumber: Long)
    suspend fun removeNotificationWhitelist(profileId: String, packageName: String, userHandleNumber: Long)
}
