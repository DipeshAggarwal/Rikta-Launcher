package com.lumina.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lumina.core.database.LuminaDatabase
import com.lumina.core.database.entity.ProfileSwitchLogEntity
import com.lumina.core.testing.builder.AppUsageSessionBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppUsageDaoTest {
    private lateinit var database: LuminaDatabase
    private lateinit var appUsageDao: AppUsageDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LuminaDatabase::class.java
        ).build()
        appUsageDao = database.appUsageDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertSession_stores_session_retrievable_via_stats() = runTest {
        val session = AppUsageSessionBuilder.build(sessionId = "test_s1")
        appUsageDao.insertSession(session)

        val stats = appUsageDao.getAppSessionStats(
            session.packageName,
            session.userHandleNumber,
            session.profileId,
            0L,
            Long.MAX_VALUE
        )

        assertNotNull(stats)
        assertEquals(1, stats!!.sessionCount)
    }

    @Test
    fun updateSessionEndTime_reflects_in_maxMs() = runTest {
        val session = AppUsageSessionBuilder.build(sessionId = "test_s1", startTime = 0L, endTime = 2048L)
        appUsageDao.insertSession(session)

        appUsageDao.updateSessionEndTime("test_s1", 8192L)
        val stats = appUsageDao.getAppSessionStats(
            session.packageName,
            session.userHandleNumber,
            session.profileId,
            0L,
            Long.MAX_VALUE
        )
        assertEquals(8192L, stats!!.maxMs)
    }

    @Test
    fun closeOrphanedSessions_closes_null_endTime_sessions() = runTest {
        val session = AppUsageSessionBuilder.build(sessionId = "test_s1", endTime = null)
        appUsageDao.insertSession(session)

        appUsageDao.closeOrphanedSessions(8192L)
        val stats = appUsageDao.getAppSessionStats(
            session.packageName,
            session.userHandleNumber,
            session.profileId,
            0L,
            Long.MAX_VALUE
        )

        assertNotNull(stats)
        assertEquals(1, stats!!.sessionCount)
    }

    @Test
    fun closeOrphanedSessions_does_not_overwrite_completed_sessions() = runTest {
        val session = AppUsageSessionBuilder.build(sessionId = "test_s1", startTime = 0L, endTime = 2048L)
        appUsageDao.insertSession(session)

        appUsageDao.closeOrphanedSessions(8192L)
        val stats = appUsageDao.getAppSessionStats(
            session.packageName,
            session.userHandleNumber,
            session.profileId,
            0L,
            Long.MAX_VALUE
        )
        assertEquals(2048L, stats!!.maxMs)
    }

    @Test
    fun getUsageStatsForProfile_returns_only_matching_profile() = runTest {
        val sessionOne = AppUsageSessionBuilder.build(
            sessionId = "test_s1", profileId = "profile_test_1",
            startTime = 0L, endTime = 2048L
        )
        val sessionTwo = AppUsageSessionBuilder.build(
            sessionId = "test_s2", profileId = "profile_test_1",
            startTime = 3072L, endTime = 5120L
        )

        appUsageDao.insertSession(sessionOne)
        appUsageDao.insertSession(sessionTwo)

        val result = appUsageDao.getUsageStatsForProfile(
            "profile_test_1", 0L, Long.MAX_VALUE
        ).first()

        assertEquals(1, result.size)
        assertEquals(4096L, result.first().totalMs)
    }

    @Test
    fun getUsageStatsForProfile_clips_session_at_range_start_boundary() = runTest {
        val session = AppUsageSessionBuilder.build(
            sessionId = "test_s1", profileId = "profile_test_1",
            startTime = 1024L, endTime = 2048L
        )
        appUsageDao.insertSession(session)

        val result = appUsageDao.getUsageStatsForProfile(
            "profile_test_1", 1280L, 2304L
        ).first()

        assertEquals(1, result.size)
        assertEquals(768L, result.first().totalMs)
    }

    @Test
    fun getUsageStatsForProfile_clips_session_at_range_end_boundary() = runTest {
        val session = AppUsageSessionBuilder.build(
            sessionId = "test_s1", profileId = "profile_test_1",
            startTime = 1024L, endTime = 2048L
        )
        appUsageDao.insertSession(session)

        val result = appUsageDao.getUsageStatsForProfile(
            "profile_test_1", 0L, 1280L
        ).first()

        assertEquals(1, result.size)
        assertEquals(256L, result.first().totalMs)
    }

    @Test
    fun getUsageStatsForProfile_excludes_sessions_outside_range() = runTest {
        val session = AppUsageSessionBuilder.build(
            sessionId = "test_s1", profileId = "profile_test_1",
            startTime = 2048L, endTime = 4096L
        )
        appUsageDao.insertSession(session)

        val result = appUsageDao.getUsageStatsForProfile(
            "profile_test_1", 0L, 1024L
        ).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getUsageStatsForProfile_excludes_sessions_with_null_endTime() = runTest {
        val session = AppUsageSessionBuilder.build(
            sessionId = "test_s1", profileId = "profile_test_1",
            startTime = 0L, endTime = null
        )
        appUsageDao.insertSession(session)

        val result = appUsageDao.getUsageStatsForProfile(
            "profile_test_1", 0L, Long.MAX_VALUE
        ).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getUsageStatsForProfile_groups_by_package_and_user() = runTest {
        val sessionOne = AppUsageSessionBuilder.build(
            sessionId = "test_s1", profileId = "profile_test_1",
            packageName = "com.rikta.test", userHandleNumber = 64L,
            startTime = 0L, endTime = 1024L
        )
        val sessionTwo = AppUsageSessionBuilder.build(
            sessionId = "test_s2", profileId = "profile_test_1",
            packageName = "com.rikta.test", userHandleNumber = 64L,
            startTime = 2048L, endTime = 3072L
        )

        appUsageDao.insertSession(sessionOne)
        appUsageDao.insertSession(sessionTwo)

        val result = appUsageDao.getUsageStatsForProfile(
            "profile_test_1", 0L, Long.MAX_VALUE
        ).first()

        assertEquals(1, result.size)
        assertEquals(2, result.first().sessionCount)
        assertEquals(2048L, result.first().totalMs)
    }

    @Test
    fun getUsageStatsForProfile_separates_same_package_different_user() = runTest {
        val sessionOne = AppUsageSessionBuilder.build(
            sessionId = "test_s1", profileId = "profile_test_1", userHandleNumber = 64L,
            startTime = 0L, endTime = 1024L
        )
        val sessionTwo = AppUsageSessionBuilder.build(
            sessionId = "test_s2", profileId = "profile_test_1", userHandleNumber = 128L,
            startTime = 2048L, endTime = 3072L
        )

        appUsageDao.insertSession(sessionOne)
        appUsageDao.insertSession(sessionTwo)

        val result = appUsageDao.getUsageStatsForProfile(
            "profile_test_1", 0L, Long.MAX_VALUE
        ).first()
        assertEquals(2, result.size)
    }

    @Test
    fun getTotalUsageAllProfiles_aggregates_across_profiles() = runTest {
        val sessionOne = AppUsageSessionBuilder.build(
            sessionId = "test_s1", profileId = "profile_test_1",
            startTime = 0L, endTime = 1024L
        )
        val sessionTwo = AppUsageSessionBuilder.build(
            sessionId = "test_s2", profileId = "profile_test_1",
            startTime = 2048L, endTime = 5120L
        )

        appUsageDao.insertSession(sessionOne)
        appUsageDao.insertSession(sessionTwo)

        val result = appUsageDao.getTotalUsageAllProfiles(0L, Long.MAX_VALUE).first()
        assertEquals(1, result.size)
        assertEquals(4096L, result.first().totalMs)
    }

    @Test
    fun getAppSessionStats_returns_null_when_no_sessions_exist() = runTest {
        val result = appUsageDao.getAppSessionStats(
            "com.rikta.unknown", 64L,
            "profile_test_1", 0L, Long.MAX_VALUE
        )
        assertNull(result)
    }

    @Test
    fun getAppSessionStats_returns_correct_max_count_and_total() = runTest {
        val sessionOne = AppUsageSessionBuilder.build(
            sessionId = "test_s1", profileId = "profile_test_1",
            startTime = 0L, endTime = 1024L
        )
        val sessionTwo = AppUsageSessionBuilder.build(
            sessionId = "test_s2", profileId = "profile_test_1",
            startTime = 2048L, endTime = 5120L
        )

        appUsageDao.insertSession(sessionOne)
        appUsageDao.insertSession(sessionTwo)

        val result = appUsageDao.getAppSessionStats(
            sessionOne.packageName, sessionOne.userHandleNumber, sessionOne.profileId,
            0L, Long.MAX_VALUE
        )
        assertNotNull(result)
        assertEquals(2, result!!.sessionCount)
        assertEquals(3072L, result.maxMs)
        assertEquals(4096L, result.totalMs)
    }

    @Test
    fun getAppUsageTotalMs_returns_null_for_no_sessions() = runTest {
        val result = appUsageDao.getAppUsageTotalMs(
            "com.rikta.test", 64L,
            "profile_test_1", 0L, Long.MAX_VALUE
        )
        assertNull(result)
    }

    @Test
    fun getAppUsageTotalMs_sums_session_durations() = runTest {
        val sessionOne = AppUsageSessionBuilder.build(
            sessionId = "test_s1", profileId = "profile_test_1",
            startTime = 0L, endTime = 1024L
        )
        val sessionTwo = AppUsageSessionBuilder.build(
            sessionId = "test_s2", profileId = "profile_test_1",
            startTime = 2048L, endTime = 3072L
        )

        appUsageDao.insertSession(sessionOne)
        appUsageDao.insertSession(sessionTwo)

        val result = appUsageDao.getAppUsageTotalMs(
            sessionOne.packageName, sessionOne.userHandleNumber, sessionOne.profileId,
            0L, Long.MAX_VALUE
        )
        assertEquals(2048L, result)
    }

    @Test
    fun getAppHourlyUsage_groups_sessions_by_hour() = runTest {
        val sessionOne = AppUsageSessionBuilder.build(
            sessionId = "test_s1", profileId = "profile_test_1",
            startTime = 3_600_000L, endTime = 3_600_000L + 1024L
        )
        val sessionTwo = AppUsageSessionBuilder.build(
            sessionId = "test_s2", profileId = "profile_test_1",
            startTime = 3_600_000L + 2048L, endTime = 3_600_000L + 3072L
        )

        appUsageDao.insertSession(sessionOne)
        appUsageDao.insertSession(sessionTwo)

        val result = appUsageDao.getAppHourlyUsage(
            sessionOne.packageName, sessionOne.userHandleNumber, sessionOne.profileId,
            0L, Long.MAX_VALUE
        )

        assertEquals(1, result.size)
        assertEquals(2048L, result.first().totalMs)
    }

    @Test
    fun hasOverlappingSession_returns_zero_for_non_overlapping_session() = runTest {
        val session = AppUsageSessionBuilder.build(sessionId = "test_s1", startTime = 0L, endTime = 1024L)
        appUsageDao.insertSession(session)

        val result = appUsageDao.hasOverlappingSession(
            session.packageName, session.userHandleNumber,
            2048L, 4096L
        )
        assertEquals(0, result)
    }

    @Test
    fun hasOverlappingSession_returns_nonzero_for_overlapping_session() = runTest {
        val session = AppUsageSessionBuilder.build(sessionId = "test_s1", startTime = 0L, endTime = 1024L)
        appUsageDao.insertSession(session)

        val result = appUsageDao.hasOverlappingSession(
            session.packageName, session.userHandleNumber,
            256L, 4096L
        )
        assertTrue(result > 0)
    }

    @Test
    fun hasOverlappingSession_returns_zero_when_sessions_touch_at_boundary() = runTest {
        val session = AppUsageSessionBuilder.build(sessionId = "test_s1", startTime = 0L, endTime = 1024L)
        appUsageDao.insertSession(session)

        val result = appUsageDao.hasOverlappingSession(
            session.packageName, session.userHandleNumber,
            1024L, 4096L
        )
        assertEquals(0, result)
    }

    @Test
    fun deleteSessionsOlderThan_removes_old_completed_sessions() = runTest {
        val sessionOne = AppUsageSessionBuilder.build(
            sessionId = "test_s1", profileId = "profile_test_1",
            startTime = 0L, endTime = 1024L
        )
        val sessionTwo = AppUsageSessionBuilder.build(
            sessionId = "test_s2", profileId = "profile_test_1",
            startTime = 2048L, endTime = 3072L
        )

        appUsageDao.insertSession(sessionOne)
        appUsageDao.insertSession(sessionTwo)
        appUsageDao.deleteSessionsOlderThan(1536L)

        val remaining = appUsageDao.getUsageStatsForProfile(
            sessionOne.profileId, sessionOne.startTime, Long.MAX_VALUE
            ).first()

        assertEquals(1, remaining.size)
        assertEquals(sessionOne.packageName, remaining.first().packageName)
        assertEquals(1024L, remaining.first().totalMs)
    }

    @Test
    fun deleteSessionsOlderThan_preserves_open_sessions() = runTest {
        val session = AppUsageSessionBuilder.build(
            sessionId = "test_s1", profileId = "profile_test_1",
            startTime = 0L, endTime = null
        )

        appUsageDao.insertSession(session)
        appUsageDao.deleteSessionsOlderThan(Long.MAX_VALUE)
        appUsageDao.updateSessionEndTime(session.sessionId, 1024L)

        val stats = appUsageDao.getAppSessionStats(
            session.packageName, session.userHandleNumber, session.profileId,
            0L, Long.MAX_VALUE
        )
        assertNotNull(stats)
    }

    @Test
    fun getActiveProfileAtTime_returns_most_recent_switch_before_timestamp() = runTest {
        val switchLogOne = ProfileSwitchLogEntity(profileId = "profile_test_1", switchedAt = 1024L)
        val switchLogTwo = ProfileSwitchLogEntity(profileId = "profile_test_2", switchedAt = 2048L)

        appUsageDao.insertSwitchLog(switchLogOne)
        appUsageDao.insertSwitchLog(switchLogTwo)

        val result = appUsageDao.getActiveProfileAtTime(1536L)
        assertNotNull(result)
        assertEquals(switchLogOne.profileId, result!!.profileId)
    }

    @Test
    fun getActiveProfileAtTime_returns_null_when_no_prior_switch_log() = runTest {
        val switchLog = ProfileSwitchLogEntity(profileId = "profile_test_1", switchedAt = 2048L)
        appUsageDao.insertSwitchLog(switchLog)

        val result = appUsageDao.getActiveProfileAtTime(1024L)
        assertNull(result)
    }

    @Test
    fun getActiveProfileAtTime_includes_exact_boundary_timestamp() = runTest {
        val switchLog = ProfileSwitchLogEntity(profileId = "profile_test_1", switchedAt = 2048L)
        appUsageDao.insertSwitchLog(switchLog)

        val result = appUsageDao.getActiveProfileAtTime(2048L)
        assertNotNull(result)
        assertEquals(switchLog.profileId, result!!.profileId)
    }

    @Test
    fun pruneOldSwitchLog_removes_entries_before_cutoff_preserving_recent() = runTest {
        val switchLogOne = ProfileSwitchLogEntity(profileId = "profile_test_1", switchedAt = 1024L)
        val switchLogTwo = ProfileSwitchLogEntity(profileId = "profile_test_2", switchedAt = 2048L)

        appUsageDao.insertSwitchLog(switchLogOne)
        appUsageDao.insertSwitchLog(switchLogTwo)
        appUsageDao.pruneOldSwitchLog(1536L)

        val result = appUsageDao.getActiveProfileAtTime(Long.MAX_VALUE)
        assertNotNull(result)
        assertEquals(switchLogTwo.profileId, result!!.profileId)
    }

    @Test
    fun pruneOldSwitchLog_removes_all_when_all_are_old() = runTest {
        val switchLog = ProfileSwitchLogEntity(profileId = "profile_test_1", switchedAt = 2048L)

        appUsageDao.insertSwitchLog(switchLog)
        appUsageDao.pruneOldSwitchLog(4096L)

        val result = appUsageDao.getActiveProfileAtTime(Long.MAX_VALUE)
        assertNull(result)
    }
}
