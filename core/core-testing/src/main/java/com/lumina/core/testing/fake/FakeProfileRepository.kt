package com.lumina.core.testing.fake

import com.lumina.core.model.AppBasicData
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.AppOverrideState
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.domain.profiles.model.TriggerCondition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map

class FakeProfileRepository : ProfileRepository {
    private val profiles = MutableStateFlow<List<LauncherProfile>>(emptyList())
    private val triggers = mutableMapOf<String, MutableStateFlow<List<TriggerCondition>>>()
    private val _activeProfile = MutableStateFlow<LauncherProfile?>(null)

    val setActiveProfileCalls = mutableListOf<String>()

    fun setProfiles(vararg profile: LauncherProfile) {
        profiles.value = profile.toList()
        profile.forEach {
            if (triggers[it.id] == null) {
                triggers[it.id] = MutableStateFlow(emptyList())
            }
        }
    }

    fun setTriggersForProfile(profileId: String, vararg trigger: TriggerCondition) {
        triggers.getOrPut(profileId) {
            MutableStateFlow<List<TriggerCondition>>(emptyList())
        }.value = trigger.toList()
    }

    override val activeProfile: Flow<LauncherProfile?> = _activeProfile

    override suspend fun setActiveProfile(profileId: String) {
        setActiveProfileCalls.add(profileId)
        _activeProfile.value = profiles.value.find { it.id == profileId }
    }

    override suspend fun clearActiveProfile() {
        _activeProfile.value = null
    }

    override fun getAllProfiles(): Flow<List<LauncherProfile>> {
        return profiles
    }

    override fun getProfileById(profileId: String): Flow<LauncherProfile?> {
        return profiles.map { it.find { it.id == profileId } }
    }

    override suspend fun isActivationKeyUnique(
        key: String,
        excludeProfileId: String
    ): Boolean {
        return true
    }

    override suspend fun findProfileByKey(key: String): LauncherProfile? {
        return profiles.value.find { it.activationKey == key }
    }

    override suspend fun saveProfile(profile: LauncherProfile) {
        profiles.value += profile
    }

    override suspend fun updateProfile(profile: LauncherProfile) {
        profiles.value = profiles.value.map { if (it.id == profile.id) profile else it }
    }

    override suspend fun deleteProfile(profileId: String) {
        profiles.value = profiles.value.filter { it.id != profileId }
    }

    override fun getAppsForProfile(profileId: String): Flow<List<AppOverrideState>> {
        return MutableStateFlow(emptyList())
    }

    override fun getAppLimitMinutes(
        profileId: String,
        packageName: String,
        userHandleNumber: Long
    ): Int? {
        return null
    }

    override suspend fun addAppToProfile(
        profileId: String,
        packageName: String,
        userHandleNumber: Long
    ) { }

    override suspend fun removeAppFromProfile(
        profileId: String,
        packageName: String,
        userHandleNumber: Long
    ) { }

    override suspend fun removeAppFromAllProfiles(
        packageName: String,
        userHandleNumber: Long
    ) { }

    override suspend fun removeAllUninstalledApps(installedKeys: Set<String>) { }

    override suspend fun updateAppOverride(
        profileId: String,
        packageName: String,
        userHandleNumber: Long,
        recommendedUsageMinutes: Int?,
        customCountdown: Int?
    ) { }

    override fun getProfileTriggers(profileId: String): Flow<List<TriggerCondition>> {
        return triggers.getOrPut(profileId) { MutableStateFlow(emptyList()) }
    }

    override suspend fun addProfileTrigger(
        profileId: String,
        conditions: TriggerCondition
    ) {
        val profileTrigger = triggers.getOrPut(profileId) { MutableStateFlow(emptyList()) }
        profileTrigger.value += conditions
    }

    override suspend fun removeProfileTrigger(triggerId: Long) {
        triggers.values.forEach { trigger ->
            trigger.value = trigger.value.filter { it.triggerId != triggerId }
        }
    }

    override suspend fun clearAllProfileTriggers(profileId: String) {
        triggers[profileId]?.value = emptyList()
    }

    override fun getNotificationWhitelist(profileId: String): Flow<Set<AppBasicData>> {
        return MutableStateFlow(emptySet())
    }

    override fun getNotificationAllowedApps(profileId: String): Flow<Set<AppBasicData>> {
        return MutableStateFlow(emptySet())
    }

    override suspend fun saveNotificationWhitelist(
        profileId: String,
        packageName: String,
        userHandleNumber: Long
    ) { }

    override suspend fun removeNotificationWhitelist(
        profileId: String,
        packageName: String,
        userHandleNumber: Long
    ) { }
}
