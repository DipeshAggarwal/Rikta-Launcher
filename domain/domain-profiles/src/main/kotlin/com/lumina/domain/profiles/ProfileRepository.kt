package com.lumina.domain.profiles

import com.lumina.core.model.AppAddedSource
import com.lumina.core.model.AppBasicData
import com.lumina.core.model.ProfileAppConfig
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.domain.profiles.model.ProfileSummary
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

    fun getAppsForProfile(profileId: String): Flow<List<ProfileAppConfig>>
    fun getProfileIdsForApp(packageName: String, userHandleNumber: Long): Flow<Set<String>>
    suspend fun setAppsForProfile(profileId: String, apps: Set<AppBasicData>)
    suspend fun addAppToProfile(
        profileId: String,
        packageName: String,
        userHandleNumber: Long,
        addedBy: AppAddedSource = AppAddedSource.USER
    )
    suspend fun addAppsToProfile(
        profileId: String,
        apps: List<AppBasicData>,
        addedBy: AppAddedSource = AppAddedSource.USER
    )
    suspend fun removeAppFromProfile(profileId: String, packageName: String, userHandleNumber: Long)
    suspend fun removeAppsFromProfile(profileId: String, apps: List<AppBasicData>)
    suspend fun removeAppFromAllProfiles(packageName: String, userHandleNumber: Long)
    suspend fun removeAllUninstalledApps(installedKeys: Set<String>)
    suspend fun removeAppsAddedByRules(profileId: String, installedKeys: Set<String>)

    suspend fun getRecommendedUsageMinutes(profileId: String, packageName: String, userHandleNumber: Long): Int?
    suspend fun updateRecommendedUsageMinutes(
        profileId: String,
        packageName: String,
        userHandleNumber: Long,
        minutes: Int?
    )

    suspend fun isShowCountdownForApp(profileId: String, packageName: String, userHandleNumber: Long): Boolean?
    suspend fun updateShowCountdownForApp(
        profileId: String,
        packageName: String,
        userHandleNumber: Long,
        show: Boolean
    )

    fun getProfileTriggers(profileId: String): Flow<List<TriggerCondition>>
    suspend fun addProfileTrigger(profileId: String, conditions: TriggerCondition)
    suspend fun updateProfileTrigger(conditions: TriggerCondition)
    suspend fun removeProfileTrigger(triggerId: Long)
    suspend fun clearAllProfileTriggers(profileId: String)

    fun getNotificationWhitelist(profileId: String): Flow<Set<AppBasicData>>
    fun getNotificationAllowedApps(profileId: String): Flow<Set<AppBasicData>>
    suspend fun saveNotificationWhitelist(profileId: String, packageName: String, userHandleNumber: Long)
    suspend fun removeNotificationWhitelist(profileId: String, packageName: String, userHandleNumber: Long)

    fun getAllProfileSummaries(): Flow<List<ProfileSummary>>
}
