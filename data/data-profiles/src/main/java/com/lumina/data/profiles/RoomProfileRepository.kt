package com.lumina.data.profiles

import com.lumina.core.database.dao.ProfileDao
import com.lumina.core.database.entity.NotificationWhitelistEntity
import com.lumina.core.database.entity.ProfileAppCrossRef
import com.lumina.core.database.entity.ProfileEntity
import com.lumina.core.database.entity.ProfileTriggerEntity
import com.lumina.core.logging.Logger
import com.lumina.core.model.AppBasicData
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.AppOverrideState
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.domain.profiles.model.TriggerCondition
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import kotlin.text.split

private const val DAYS_DELIMITER = ","

class RoomProfileRepository @Inject constructor(
    private val profileDao: ProfileDao,
    private val profileDataStore: ProfileDataStore,
    private val logger: Logger
) : ProfileRepository {
    private val TAG = this::class.java.simpleName

    private fun String.hash(): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun ProfileEntity.toDomain() = LauncherProfile(
        id = id,
        userHandleNumber = userHandleNumber,
        type = type,
        name = name,
        strictMode = strictMode,
        priorityTriggerLaunch = priorityTriggerLaunch,
        filterNotification = filterNotification,
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

    private fun LauncherProfile.toEntity(hashedKey: String?) = ProfileEntity(
        id = id,
        userHandleNumber = userHandleNumber,
        type = type,
        name = name,
        strictMode = strictMode,
        priorityTriggerLaunch = priorityTriggerLaunch,
        filterNotification = filterNotification,
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
        activationKey = hashedKey,
    )

    private fun ProfileTriggerEntity.toDomain() = TriggerCondition(
        triggerId = triggerId,
        logicalOperator = logicalOperator,
        triggerType = triggerType,
        stopIfTrue = stopIfTrue,
        startTimeMinutes = startTimeMinutes,
        endTimeMinutes = endTimeMinutes,
        daysOfWeek = daysOfWeek?.split(DAYS_DELIMITER)?.mapNotNull { it.toIntOrNull() },
        latitude = latitude,
        longitude = longitude,
        radiusMeters = radiusMeters,
        wifiSsid = wifiSsid,
        bluetoothAddress = bluetoothAddress
    )

    private fun TriggerCondition.toEntity(profileId: String, sequenceOrder: Int = 0) = ProfileTriggerEntity(
        triggerId = triggerId,
        profileId = profileId,
        sequenceOrder = sequenceOrder,
        logicalOperator = logicalOperator,
        triggerType = triggerType,
        stopIfTrue = stopIfTrue,
        startTimeMinutes = startTimeMinutes,
        endTimeMinutes = endTimeMinutes,
        daysOfWeek = daysOfWeek?.joinToString(DAYS_DELIMITER),
        latitude = latitude,
        longitude = longitude,
        radiusMeters = radiusMeters,
        wifiSsid = wifiSsid,
        bluetoothAddress = bluetoothAddress
    )

    // ------------------------------------------------
    // Active Profile

    @OptIn(ExperimentalCoroutinesApi::class)
    override val activeProfile: Flow<LauncherProfile?> = profileDataStore.activeProfileId
        .flatMapLatest { id ->
            if (id == null) return@flatMapLatest flowOf(null)
            profileDao.getProfileById(id).map { it?.toDomain() }
        }

    override suspend fun setActiveProfile(profileId: String) {
        profileDataStore.setActiveProfileId(profileId)
    }

    override suspend fun clearActiveProfile() {
        profileDataStore.setActiveProfileId(null)
    }

    // ------------------------------------------------
    // Profile Operations

    override fun getAllProfiles(): Flow<List<LauncherProfile>> {
        return profileDao.getAllProfiles().map { list -> list.map { it.toDomain() } }
    }

    override fun getProfileById(profileId: String): Flow<LauncherProfile?> {
        return profileDao.getProfileById(profileId).map { it?.toDomain() }
    }

    override suspend fun isActivationKeyUnique(
        key: String,
        excludeProfileId: String
    ): Boolean {
        return !profileDao.isKeyTaken(key.hash(), excludeProfileId)
    }

    override suspend fun findProfileByKey(key: String): LauncherProfile? {
        if (key.isBlank()) return null
        return profileDao.getProfileByActivationKey(key.hash())?.toDomain()
    }

    override suspend fun saveProfile(profile: LauncherProfile) {
        val hashedKey = profile.activationKey?.hash()

        if (hashedKey != null && profileDao.isKeyTaken(hashedKey, profile.id)) {
            logger.w(
                TAG,
                "This PIN is already being used.",
                IllegalArgumentException("This PIN is already being used.")
            )
            throw IllegalArgumentException("This PIN is already being used.")
        }
        profileDao.saveProfile(profile.toEntity(hashedKey))
    }

    override suspend fun updateProfile(profile: LauncherProfile) {
        val hashedKey = profile.activationKey?.hash()

        if (hashedKey != null && profileDao.isKeyTaken(hashedKey, profile.id)) {
            logger.w(
                TAG,
                "This PIN is already being used.",
                IllegalArgumentException("This PIN is already being used.")
            )
            throw IllegalArgumentException("This PIN is already being used.")
        }
        profileDao.updateProfile(profile.toEntity(hashedKey))
    }

    override suspend fun deleteProfile(profileId: String) {
        profileDao.deleteProfileById(profileId)
    }

    // ------------------------------------------------
    // Profile Specific Apps

    override fun getAppsForProfile(profileId: String): Flow<List<AppOverrideState>> {
        return profileDao.getAppsForProfile(profileId).map { apps ->
            apps.map { app ->
                AppOverrideState(
                    appBasicData = AppBasicData(app.packageName, app.userHandleNumber),
                    customDisplayName = app.customDisplayName,
                    recommendedUsageMinutes = app.recommendedUsageMinutes,
                    customCountdown = app.customCountdown
                )
            }
        }
    }

    override suspend fun addAppToProfile(
        profileId: String,
        packageName: String,
        userHandleNumber: Long
    ) {
        profileDao.insertAppMapping(ProfileAppCrossRef(profileId, packageName, userHandleNumber))
    }

    override suspend fun removeAppFromProfile(
        profileId: String,
        packageName: String,
        userHandleNumber: Long
    ) {
        profileDao.deleteAppMapping(profileId, packageName, userHandleNumber)
    }

    override suspend fun updateAppOverride(
        profileId: String,
        packageName: String,
        userHandleNumber: Long,
        customName: String?,
        recommendedUsageMinutes: Int?,
        customCountdown: Int?
    ) {
        profileDao.insertAppMapping(ProfileAppCrossRef(
            profileId, packageName, userHandleNumber,
            customName, recommendedUsageMinutes, customCountdown
        ))
    }

    // ------------------------------------------------
    // Profile Trigger

    override fun getProfileTriggers(profileId: String): Flow<List<TriggerCondition>> {
        return profileDao.getTriggersForProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addProfileTrigger(
        profileId: String,
        conditions: TriggerCondition
    ) {
        profileDao.insertTriggerWithOrder(conditions.toEntity(profileId))
    }

    override suspend fun removeProfileTrigger(triggerId: Long) {
        profileDao.deleteTrigger(triggerId)
    }

    override suspend fun clearAllProfileTriggers(profileId: String) {
        profileDao.clearTriggers(profileId)
    }

    // ------------------------------------------------
    // Profile Notification Control

    override fun getNotificationWhitelist(profileId: String): Flow<Set<AppBasicData>> {
        return profileDao.getNotificationWhitelist(profileId).map {apps ->
            apps.map { AppBasicData(it.packageName, it.userHandleNumber) }.toSet()
        }
    }

    override fun getNotificationAllowedApps(profileId: String): Flow<Set<AppBasicData>> {
        return combine(
            profileDao.getAppsForProfile(profileId),
            profileDao.getNotificationWhitelist(profileId)
        ) { profileApps, manualWhitelistApps ->
            val allowedAppsSet = mutableSetOf<AppBasicData>()

            profileApps.forEach {
                allowedAppsSet.add(AppBasicData(it.packageName, it.userHandleNumber))
            }
            manualWhitelistApps.forEach {
                allowedAppsSet.add(AppBasicData(it.packageName, it.userHandleNumber))
            }

            allowedAppsSet
        }
    }

    override suspend fun saveNotificationWhitelist(
        profileId: String,
        packageName: String,
        userHandleNumber: Long
    ) {
        profileDao.insertWhitelist(
            NotificationWhitelistEntity(profileId, packageName, userHandleNumber)
        )
    }

    override suspend fun removeNotificationWhitelist(
        profileId: String,
        packageName: String,
        userHandleNumber: Long
    ) {
        profileDao.deleteWhitelist(
            NotificationWhitelistEntity(profileId, packageName, userHandleNumber)
        )
    }
}
