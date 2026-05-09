package com.lumina.data.profiles

import androidx.room.withTransaction
import com.lumina.core.database.LuminaDatabase
import com.lumina.core.database.dao.ProfileDao
import com.lumina.core.database.entity.NotificationWhitelistEntity
import com.lumina.core.database.entity.ProfileAppCrossRef
import com.lumina.core.database.entity.ProfileEntity
import com.lumina.core.database.entity.ProfileOverrides
import com.lumina.core.database.entity.ProfileSettings
import com.lumina.core.database.entity.ProfileTriggerEntity
import com.lumina.core.logging.Logger
import com.lumina.core.model.AppBasicData
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.core.model.ProfileAppConfig
import com.lumina.core.model.componentKey
import com.lumina.core.model.getAppKey
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.domain.profiles.model.LauncherProfileSettings
import com.lumina.domain.profiles.model.LauncherProfileOverrides
import com.lumina.domain.profiles.model.ProfileSummary
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
    private val database: LuminaDatabase,
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
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
        settings = LauncherProfileSettings(
            strictMode = settings.strictMode,
            isAdmin = settings.isAdmin,
            priorityTriggerLaunch = settings.priorityTriggerLaunch,
            filterNotification = settings.filterNotification,
            blockProfileTriggerSwitching = settings.blockProfileTriggerSwitching,
            allowAppRename = settings.allowAppRename,
            allowProfileManagement = settings.allowProfileManagement,
            allowAppCategoryChange = settings.allowAppCategoryChange,
            startDnd = settings.startDnd,
            showAppList = settings.showAppList,
            hideScreenTimeOnApps = settings.hideScreenTimeOnApps,
            disableOnLock = settings.disableOnLock,
            blockUnauthorisedApps = settings.blockUnauthorisedApps,
            entryAuthMethod = settings.entryAuthMethod,
            exitAuthMethod = settings.exitAuthMethod,
            activationKey = settings.activationKey,
        ),
        overrides = LauncherProfileOverrides(
            theme = overrides.theme,
            background = overrides.background,
            font = overrides.font,
            showClock = overrides.showClock,
            showBigClock = overrides.showBigClock,
            showDate = overrides.showDate,
            showWeather = overrides.showWeather,
            hideScreenTime = overrides.hideScreenTime,
            iconName = overrides.iconName,
        )
    )

    private fun LauncherProfile.toEntity(hashedKey: String?) = ProfileEntity(
        id = id,
        userHandleNumber = userHandleNumber,
        type = type,
        name = name,
        description = description,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis(),
        settings = ProfileSettings(
            strictMode = settings.strictMode,
            isAdmin = settings.isAdmin,
            priorityTriggerLaunch = settings.priorityTriggerLaunch,
            filterNotification = settings.filterNotification,
            blockProfileTriggerSwitching = settings.blockProfileTriggerSwitching,
            allowAppRename = settings.allowAppRename,
            allowProfileManagement = settings.allowProfileManagement,
            allowAppCategoryChange = settings.allowAppCategoryChange,
            startDnd = settings.startDnd,
            showAppList = settings.showAppList,
            hideScreenTimeOnApps = settings.hideScreenTimeOnApps,
            disableOnLock = settings.disableOnLock,
            blockUnauthorisedApps = settings.blockUnauthorisedApps,
            entryAuthMethod = settings.entryAuthMethod,
            exitAuthMethod = settings.exitAuthMethod,
            activationKey = settings.activationKey,
        ),
        overrides = ProfileOverrides(
            theme = overrides.theme,
            background = overrides.background,
            font = overrides.font,
            showClock = overrides.showClock,
            showBigClock = overrides.showBigClock,
            showDate = overrides.showDate,
            showWeather = overrides.showWeather,
            hideScreenTime = overrides.hideScreenTime,
            iconName = overrides.iconName,
        )
    )

    private fun ProfileTriggerEntity.toDomain() = TriggerCondition(
        triggerId = triggerId,
        profileId = profileId,
        sequenceOrder = sequenceOrder,
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
        val hashedKey = profile.settings.activationKey?.hash()

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
        val hashedKey = profile.settings.activationKey?.hash()

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

    override fun getAppsForProfile(profileId: String): Flow<List<ProfileAppConfig>> {
        return profileDao.getAppsForProfile(profileId).map { apps ->
            apps.map { app ->
                ProfileAppConfig(
                    appBasicData = AppBasicData(app.packageName, app.userHandleNumber),
                    showCountdown = app.showCountdown,
                    recommendedUsageMinutes = app.recommendedUsageMinutes,
                )
            }
        }
    }

    override fun getProfileIdsForApp(
        packageName: String,
        userHandleNumber: Long
    ): Flow<Set<String>> {
        return profileDao.getProfileIdsForApp(packageName, userHandleNumber).map { it.toSet() }
    }

    override suspend fun setAppsForProfile(
        profileId: String,
        apps: Set<AppBasicData>
    ) = database.withTransaction {
        val currentMappings = profileDao.getAppsForProfileSnapshot(profileId)
        val currentKeys = currentMappings.map { getAppKey(it.packageName, it.userHandleNumber) }.toSet()
        val newApps = apps.map { it.componentKey }.toSet()

        val appsToAdd = apps.filter { it.componentKey !in currentKeys }
        val appsToRemove = currentMappings.filter { getAppKey(it.packageName, it.userHandleNumber) !in newApps }

        if (appsToRemove.isNotEmpty()) {
            profileDao.deleteAppMappings(appsToRemove)
        }
        if (appsToAdd.isNotEmpty()) {
            addAppsToProfile(profileId, appsToAdd)
        }
    }

    override suspend fun addAppToProfile(
        profileId: String,
        packageName: String,
        userHandleNumber: Long
    ) {
        profileDao.insertAppMapping(ProfileAppCrossRef(profileId, packageName, userHandleNumber))
    }

    override suspend fun addAppsToProfile(
        profileId: String,
        apps: List<AppBasicData>
    ) {
        val mappings = apps.map { app ->
            ProfileAppCrossRef(profileId, app.packageName, app.userHandleNumber)
        }
        profileDao.insertAppMappings(mappings)
    }

    override suspend fun removeAppFromProfile(
        profileId: String,
        packageName: String,
        userHandleNumber: Long
    ) {
        profileDao.deleteAppMapping(profileId, packageName, userHandleNumber)
    }

    override suspend fun removeAppsFromProfile(
        profileId: String,
        apps: List<AppBasicData>
    ) {
        val mappings = apps.map { app ->
            ProfileAppCrossRef(profileId, app.packageName, app.userHandleNumber)
        }
        profileDao.deleteAppMappings(mappings)
    }

    override suspend fun removeAppFromAllProfiles(packageName: String, userHandleNumber: Long) {
        profileDao.deleteAppMappingAcrossAllProfiles(packageName, userHandleNumber)
    }

    override suspend fun removeAllUninstalledApps(installedKeys: Set<String>) {
        profileDao.deleteAppMappingForUninstalledApps(installedKeys)
    }

    override suspend fun getRecommendedUsageMinutes(
        profileId: String,
        packageName: String,
        userHandleNumber: Long
    ): Int? {
        return profileDao.getAppUsageMinutes(profileId, packageName, userHandleNumber)
    }

    override suspend fun updateRecommendedUsageMinutes(
        profileId: String,
        packageName: String,
        userHandleNumber: Long,
        minutes: Int?
    ) = database.withTransaction {
        profileDao.insertAppMapping(ProfileAppCrossRef(profileId, packageName, userHandleNumber))
        profileDao.updateAppUsageMinutes(profileId, packageName, userHandleNumber, minutes)
    }

    override suspend fun isShowCountdownForApp(
        profileId: String,
        packageName: String,
        userHandleNumber: Long
    ): Boolean? {
        return profileDao.isAppCountdownEnabled(profileId, packageName, userHandleNumber)
    }

    override suspend fun updateShowCountdownForApp(
        profileId: String,
        packageName: String,
        userHandleNumber: Long,
        show: Boolean
    ) = database.withTransaction {
        profileDao.insertAppMapping(ProfileAppCrossRef(profileId, packageName, userHandleNumber))
        profileDao.updateAppCountdown(profileId, packageName, userHandleNumber, show)
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

    override suspend fun updateProfileTrigger(conditon: TriggerCondition) {
        profileDao.insertTrigger(conditon.toEntity(conditon.profileId, conditon.sequenceOrder))
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
        profileDao.insertNotificationWhitelist(
            NotificationWhitelistEntity(profileId, packageName, userHandleNumber)
        )
    }

    override suspend fun removeNotificationWhitelist(
        profileId: String,
        packageName: String,
        userHandleNumber: Long
    ) {
        profileDao.deleteNotificationWhitelist(
            NotificationWhitelistEntity(profileId, packageName, userHandleNumber)
        )
    }

    override fun getAllProfileSummaries(): Flow<List<ProfileSummary>> {
        return profileDao.getAllProfileSummaries().map { summaries ->
            summaries.map { result ->
                ProfileSummary(
                    profile = result.profile.toDomain(),
                    appCount = result.appCount,
                    triggerCount = result.triggerCount,
                    allowedNotificationCount = result.allowedNotificationCount
                )
            }
        }
    }
}
