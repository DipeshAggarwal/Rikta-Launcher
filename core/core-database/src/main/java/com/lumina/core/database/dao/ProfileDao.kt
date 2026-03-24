package com.lumina.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.lumina.core.database.entity.NotificationWhitelistEntity
import com.lumina.core.database.entity.ProfileAppCrossRef
import com.lumina.core.database.entity.ProfileEntity
import com.lumina.core.database.entity.ProfileTriggerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    // ------------------------------------------------
    // Active Profile

    @Query("SELECT * FROM profiles")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :id")
    fun getProfileById(id: String): Flow<ProfileEntity?>

    // ------------------------------------------------
    // Profile Operations

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: ProfileEntity)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :profileId")
    suspend fun deleteProfileById(profileId: String)

    // ------------------------------------------------
    // Profile Authentication

    @Query("SELECT * FROM profiles WHERE activationKey = :hashedKey LIMIT 1")
    suspend fun getProfileByActivationKey(hashedKey: String): ProfileEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM profiles WHERE activationKey = :hashedKey AND id != :excludeId)")
    suspend fun isKeyTaken(hashedKey: String, excludeId: String): Boolean

    // ------------------------------------------------
    // Profile Specific Apps

    @Query("SELECT * FROM profile_app_mapping WHERE profileId = :profileId")
    fun getAppsForProfile(profileId: String): Flow<List<ProfileAppCrossRef>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAppMapping(mapping: ProfileAppCrossRef)

    @Query("SELECT * FROM profile_app_mapping " +
            "WHERE profileId = :profileId " +
            "AND packageName = :packageName " +
            "AND userHandleNumber = :userHandleNumber"
    )
    suspend fun getAppMapping(profileId: String, packageName: String, userHandleNumber: Long): ProfileAppCrossRef?

    @Query("DELETE FROM profile_app_mapping " +
        "WHERE profileId = :profileId " +
        "AND packageName = :packageName " +
        "AND userHandleNumber = :userHandleNumber "
    )
    suspend fun deleteAppMapping(profileId: String, packageName: String, userHandleNumber: Long)

    @Query("DELETE FROM profile_app_mapping " +
            "WHERE packageName = :packageName " +
            "AND userHandleNumber = :userHandleNumber"
    )
    suspend fun deleteAppMappingAcrossAllProfiles(packageName: String, userHandleNumber: Long)

    @Query("DELETE FROM profile_app_mapping " +
            "WHERE (packageName || '::' || userHandleNumber) NOT IN (:installedKeys)"
    )
    suspend fun deleteAppMappingForUninstalledApps(installedKeys: Set<String>)

    // ------------------------------------------------
    // Recommended Usage Apps

    @Query("SELECT recommendedUsageMinutes " +
            "FROM profile_app_mapping " +
            "WHERE profileId = :profileId " +
            "AND packageName = :packageName " +
            "AND userHandleNumber = :userHandleNumber"
    )
    suspend fun getAppUsageMinutes(profileId: String, packageName: String, userHandleNumber: Long): Int?

    @Query("UPDATE profile_app_mapping " +
            "SET recommendedUsageMinutes = :minutes " +
            "WHERE profileId = :profileId " +
            "AND packageName = :packageName " +
            "AND userHandleNumber = :userHandleNumber"
    )
    suspend fun updateAppUsageMinutes(
        profileId: String,
        packageName: String,
        userHandleNumber: Long,
        minutes: Int?
    )

    // ------------------------------------------------
    // Countdown Apps

    @Query("SELECT showCountdown " +
            "FROM profile_app_mapping " +
            "WHERE profileId = :profileId " +
            "AND packageName = :packageName " +
            "AND userHandleNumber = :userHandleNumber"
    )
    suspend fun isAppCountdownEnabled(profileId: String, packageName: String, userHandleNumber: Long): Boolean?

    @Query("UPDATE profile_app_mapping " +
            "SET showCountdown = :show " +
            "WHERE profileId = :profileId " +
            "AND packageName = :packageName " +
            "AND userHandleNumber = :userHandleNumber"
    )
    suspend fun updateAppCountdown(
        profileId: String,
        packageName: String,
        userHandleNumber: Long,
        show: Boolean
    )

    // ------------------------------------------------
    // Favourite Apps

    @Query("SELECT * FROM profile_app_mapping " +
            "WHERE profileId = :profileId " +
            "AND favouriteOrder IS NOT NULL " +
            "ORDER BY favouriteOrder ASC"
    )
    fun getFavouriteApps(profileId: String): Flow<List<ProfileAppCrossRef>>

    @Query("SELECT MAX(favouriteOrder) FROM profile_app_mapping WHERE profileId = :profileId")
    suspend fun getMaxFavouriteOrder(profileId: String): Int?

    @Query("SELECT favouriteOrder FROM profile_app_mapping " +
            "WHERE profileId = :profileId " +
            "AND packageName = :packageName " +
            "AND userHandleNumber = :userHandleNumber"
    )
    suspend fun getOrderForApp(profileId: String, packageName: String, userHandleNumber: Long): Int?

    @Query("UPDATE profile_app_mapping SET favouriteOrder = :order " +
            "WHERE profileId = :profileId " +
            "AND packageName = :packageName " +
            "AND userHandleNumber = :userHandleNumber"
    )
    suspend fun updateFavouriteAppOrder(
        profileId: String,
        packageName: String,
        userHandleNumber: Long,
        order: Int?
    )

    @Transaction
    suspend fun toggleFavouriteApp(
        profileId: String,
        packageName: String,
        userHandleNumber: Long,
        gap: Int
    ) {
        insertAppMapping(ProfileAppCrossRef(profileId, packageName, userHandleNumber))
        val currentFavouriteOrder = getOrderForApp(profileId, packageName, userHandleNumber)

        if (currentFavouriteOrder != null) {
            updateFavouriteAppOrder(profileId, packageName, userHandleNumber, null)
        } else {
            val max = getMaxFavouriteOrder(profileId) ?: 0
            updateFavouriteAppOrder(profileId, packageName, userHandleNumber, max + gap)
        }
    }

    @Transaction
    suspend fun rebalanceFavouriteApps(profileId: String, apps: List<ProfileAppCrossRef>, gap: Int) {
        apps.forEachIndexed { index, app ->
            updateFavouriteAppOrder(profileId, app.packageName, app.userHandleNumber, (index + 1) * gap)
        }
    }

    // ------------------------------------------------
    // Profile Trigger

    @Query("SELECT * FROM profile_triggers WHERE profileId = :profileId  ORDER BY sequenceOrder")
    fun getTriggersForProfile(profileId: String): Flow<List<ProfileTriggerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrigger(trigger: ProfileTriggerEntity)

    @Transaction
    suspend fun insertTriggerWithOrder(trigger: ProfileTriggerEntity) {
        val count = getTriggerCount(trigger.profileId)
        insertTrigger(trigger.copy(sequenceOrder = count))
    }

    @Query("DELETE FROM profile_triggers WHERE triggerId = :triggerId")
    suspend fun deleteTrigger(triggerId: Long)

    @Query("DELETE FROM profile_triggers WHERE profileId = :profileId")
    suspend fun clearTriggers(profileId: String)

    @Query("SELECT COUNT(*) FROM profile_triggers WHERE profileId = :profileId")
    suspend fun getTriggerCount(profileId: String): Int

    // ------------------------------------------------
    // Profile Notification Control

    @Query("SELECT * FROM profile_notification_whitelist WHERE profileId = :profileId")
    fun getNotificationWhitelist(profileId: String): Flow<List<NotificationWhitelistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationWhitelist(entry: NotificationWhitelistEntity)

    @Delete
    suspend fun deleteNotificationWhitelist(entry: NotificationWhitelistEntity)
}
