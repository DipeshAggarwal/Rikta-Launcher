package com.lumina.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lumina.core.database.entity.AppUsageSessionEntity
import com.lumina.core.database.entity.ProfileSwitchLogEntity
import com.lumina.core.database.models.AppSessionStatesRow
import com.lumina.core.database.models.AppUsageHourlyRow
import com.lumina.core.database.models.AppUsageStatsRow
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    // ------------------------------------------------
    // Session write

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AppUsageSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSwitchLog(log: ProfileSwitchLogEntity)

    @Query("UPDATE app_usage_sessions SET endTime = :rebootTime WHERE endTime IS NULL")
    suspend fun closeOrphanedSessions(rebootTime: Long)

    @Query("UPDATE app_usage_sessions SET endTime = :endTime WHERE sessionId = :sessionId")
    suspend fun updateSessionEndTime(sessionId: String, endTime: Long)

    // ------------------------------------------------
    // Usage sessions stats

    @Query("""
        SELECT packageName, userHandleNumber, 
        SUM(MIN(endTime, :endMs) - MAX(startTime, :startMs)) AS totalMs,
        MAX(endTime - startTime) AS maxMs,
        COUNT(sessionId) AS sessionCount
        FROM app_usage_sessions
        WHERE profileId = :profileId
        AND endTime IS NOT NULL
        AND startTime < :endMs
        AND endTime > :startMs
        GROUP BY packageName, userHandleNumber
    """)
    fun getUsageStatsForProfile(profileId: String, startMs: Long, endMs: Long): Flow<List<AppUsageStatsRow>>

    // ------------------------------------------------
    // Usage sessions stats - all profiles

    @Query("""
        SELECT packageName, userHandleNumber,
        SUM(MIN(endTime, :endMs) - MAX(startTime, :startMs)) AS totalMs,
        MAX(endTime - startTime) AS maxMs,
        COUNT(sessionId) AS sessionCount
        FROM app_usage_sessions
        WHERE endTime IS NOT NULL
        AND startTime < :endMs
        AND endTime > :startMs
        GROUP BY packageName, userHandleNumber
    """)
    fun getTotalUsageAllProfiles(startMs: Long, endMs: Long): Flow<List<AppUsageStatsRow>>

    // ------------------------------------------------
    // Usage sessions stats - One App

    @Query("""
        SELECT MAX(endTime - startTime) AS maxMs,
        COUNT(*) AS sessionCount,
        SUM(MIN(endTime, :endMs) - MAX(startTime, :startMs)) AS totalMs
        FROM app_usage_sessions
        WHERE packageName = :packageName
        AND userHandleNumber = :userHandleNumber
        AND profileId = :profileId
        AND endTime IS NOT NULL
        AND startTime < :endMs
        AND endTime > :startMs
        GROUP BY packageName
    """)
    suspend fun getAppSessionStats(
        packageName: String,
        userHandleNumber: Long,
        profileId: String,
        startMs: Long,
        endMs: Long
    ): AppSessionStatesRow?

    @Query("""
        SELECT SUM(MIN(endTime, :endMs) - MAX(startTime, :startMs))
        FROM app_usage_sessions
        WHERE packageName = :packageName
        AND userHandleNumber = :userHandleNumber
        AND profileId = :profileId
        AND endTime IS NOT NULL
        AND startTime < :endMs
        AND endTime > :startMs
    """)
    suspend fun getAppUsageTotalMs(
        packageName: String,
        userHandleNumber: Long,
        profileId: String,
        startMs: Long,
        endMs: Long
    ): Long?

    // ------------------------------------------------
    // Usage sessions stats - One App by hour

    @Query("""
        SELECT CAST(strftime('%H', startTime / 1000, 'unixepoch', 'localtime') AS INTEGER) AS hourOfDay,
        SUM(MIN(endTime, :endMs) - MAX(startTime, :startMs)) AS totalMs
        FROM app_usage_sessions
        WHERE packageName = :packageName
        AND userHandleNumber = :userHandleNumber
        AND profileId = :profileId
        AND endTime IS NOT NULL
        AND startTime < :endMs
        AND endTime > :startMs
        GROUP BY hourOfDay
        ORDER BY hourOfDay
    """)
    suspend fun getAppHourlyUsage(
        packageName: String,
        userHandleNumber: Long,
        profileId: String,
        startMs: Long,
        endMs: Long
    ): List<AppUsageHourlyRow>

    // ------------------------------------------------
    // Detect Gap

    @Query("""
        SELECT COUNT(*) FROM app_usage_sessions
        WHERE packageName = :packageName
        AND userHandleNumber = :userHandleNumber
        AND startTime < :endTime
        AND endTime > :startTime
    """)
    suspend fun hasOverlappingSession(
        packageName: String,
        userHandleNumber: Long,
        startTime: Long,
        endTime: Long
    ): Int

    // ------------------------------------------------
    // Delete Sessions

    @Query("""
        DELETE FROM app_usage_sessions
        WHERE endTime IS NOT NULL
        AND startTime < :cutoffTime
    """)
    suspend fun deleteSessionsOlderThan(cutoffTime: Long)

    // ------------------------------------------------
    // Profile Switching Log

    @Query("""
        SELECT * FROM profile_switch_log
        WHERE switchedAt <= :timestamp
        ORDER BY switchedAt DESC
        LIMIT 1
    """)
    suspend fun getActiveProfileAtTime(timestamp: Long): ProfileSwitchLogEntity?

    @Query("DELETE FROM profile_switch_log WHERE switchedAt < :cutoff")
    suspend fun pruneOldSwitchLog(cutoff: Long)
}
