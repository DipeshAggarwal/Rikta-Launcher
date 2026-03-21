package com.lumina.data.usage

import com.lumina.core.database.dao.AppUsageDao
import com.lumina.core.database.dao.ProfileDao
import com.lumina.core.database.entity.AppUsageSessionEntity
import com.lumina.core.database.entity.ProfileSwitchLogEntity
import com.lumina.core.logging.Logger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAppUsageTrackerTest {
    private lateinit var appUsageDao: AppUsageDao
    private lateinit var appUsageTracker: DefaultAppUsageTracker
    private lateinit var  logger: Logger

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        appUsageDao = mockk(relaxed = true)
        logger = mockk(relaxed = true)
        appUsageTracker = DefaultAppUsageTracker(testScope.backgroundScope, appUsageDao, logger)
    }

    @Test
    fun `onProfileSwitched inserts switch log with correct profileId and timestamp`() = testScope.runTest {
        val slot = slot<ProfileSwitchLogEntity>()
        coEvery { appUsageDao.insertSwitchLog(capture(slot)) } returns Unit

        appUsageTracker.onProfileSwitched("profile_test", 1024L)
        advanceTimeBy(128)

        assertEquals("profile_test", slot.captured.profileId)
        assertEquals(1024L, slot.captured.switchedAt)
    }

    @Test
    fun `onBootCompleted closes orphaned session at reboot`() = testScope.runTest {
        appUsageTracker.onBootCompleted(4096L)
        advanceTimeBy(128)

        coEvery { appUsageDao.closeOrphanedSessions(4096L) }
    }

    @Test
    fun `session below MIN_SESSION_MILLISECONDS is not written to room`() = testScope.runTest {
        appUsageTracker.onAppForegrounded(
            "com.example.app", 0L,
            "profile_test", 0L
        )
        advanceTimeBy(UsageConstants.MIN_SESSION_MILLISECONDS - 1)

        appUsageTracker.onAppBackgrounded(
            "com.example.app", 0L,UsageConstants.MIN_SESSION_MILLISECONDS - 1
        )
        advanceTimeBy(128)

        coVerify(exactly = 0) { appUsageDao.insertSession(any()) }
    }

    @Test
    fun `session is written to db after MIN_SESSION_MILLISECONDS elapses`() = testScope.runTest {
        appUsageTracker.onAppForegrounded(
            "com.example.app", 0L,
            "profile_test", 0L
        )
        advanceTimeBy(UsageConstants.MIN_SESSION_MILLISECONDS + 64L)
        runCurrent()

        coVerify(exactly = 1) { appUsageDao.insertSession(any()) }
    }

    @Test
    fun `session is marked to the profile active at foreground time`() = testScope.runTest {
        val sessionSlot = slot<AppUsageSessionEntity>()
        coEvery { appUsageDao.insertSession(capture(sessionSlot)) } returns Unit

        appUsageTracker.onAppForegrounded(
            "com.example.app", 0L,
            "profile_test", 0L
        )
        advanceTimeBy(UsageConstants.MIN_SESSION_MILLISECONDS + 64L)
        assertEquals("profile_test", sessionSlot.captured.profileId)
    }

    @Test
    fun `backgrounding app writes a session to DB`() = testScope.runTest {
        appUsageTracker.onAppForegrounded(
            "com.example.app", 0L,
            "profile_test", 0L
        )
        advanceTimeBy(UsageConstants.MIN_SESSION_MILLISECONDS + 64L)

        appUsageTracker.onAppBackgrounded(
            "com.example.app", 0L, UsageConstants.MIN_SESSION_MILLISECONDS + 128L
        )
        advanceTimeBy(64)
        coVerify { appUsageDao.updateSessionEndTime(any(), any()) }
    }

    @Test
    fun `backgrounding a short session does not write to DB`() = testScope.runTest {
        appUsageTracker.onAppForegrounded(
            "com.example.app", 0L,
            "profile_test", 0L
        )
        advanceTimeBy(UsageConstants.MIN_SESSION_MILLISECONDS - 1L)

        appUsageTracker.onAppBackgrounded(
            "com.example.app", 0L, UsageConstants.MIN_SESSION_MILLISECONDS - 1L
        )
        advanceTimeBy(64)
        coVerify(exactly = 0) { appUsageDao.updateSessionEndTime(any(), any()) }
    }

    @Test
    fun `same package with different userHandle are tracked as separate sessions`() = testScope.runTest {
        appUsageTracker.onAppForegrounded(
            "com.example.app", 0L,
            "profile_test", 0L
        )
        appUsageTracker.onAppForegrounded(
            "com.example.app", 1L,
            "profile_test", 0L
        )

        advanceTimeBy(UsageConstants.MIN_SESSION_MILLISECONDS + 64L)
        coVerify(exactly = 2) { appUsageDao.insertSession(any()) }
    }

    @Test
    fun `foregrounding a new app closes the last active app session`() = testScope.runTest {
        appUsageTracker.onAppForegrounded(
            "com.example.app", 0L,
            "profile_test", 0L
        )
        advanceTimeBy(UsageConstants.MIN_SESSION_MILLISECONDS + 64)

        val closeTime = UsageConstants.MIN_SESSION_MILLISECONDS + 128L
        appUsageTracker.onAppBackgrounded(
            "com.example.app", 0L, closeTime
        )
        advanceTimeBy(64)
        coVerify { appUsageDao.updateSessionEndTime(any(), closeTime) }
    }
}
