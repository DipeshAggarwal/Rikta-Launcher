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

    @Query("SELECT recommendedUsageMinutes " +
            "FROM profile_app_mapping " +
            "WHERE profileId = :profileId " +
            "AND packageName = :packageName " +
            "AND userHandleNumber = :userHandleNumber"
    )
    fun getAppLimitMinutes(profileId: String, packageName: String, userHandleNumber: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppMapping(mapping: ProfileAppCrossRef)

    @Query("DELETE FROM profile_app_mapping " +
        "WHERE profileId = :profileId " +
        "AND packageName = :packageName " +
        "AND userHandleNumber = :userHandleNumber "
    )
    suspend fun deleteAppMapping(profileId: String, packageName: String, userHandleNumber: Long)

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
    suspend fun insertWhitelist(entry: NotificationWhitelistEntity)

    @Delete
    suspend fun deleteWhitelist(entry: NotificationWhitelistEntity)
}
