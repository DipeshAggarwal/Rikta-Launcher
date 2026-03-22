package com.lumina.data.usage

import com.lumina.core.database.dao.AppUsageDao
import com.lumina.core.database.entity.AppUsageSessionEntity
import com.lumina.core.database.entity.ProfileSwitchLogEntity
import com.lumina.core.logging.Logger
import com.lumina.core.testing.fake.FakeTimeProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TestHeartbeatTicker : HeartbeatTicker {
    private val _ticks = MutableSharedFlow<Unit>(extraBufferCapacity = 8)

    suspend fun tick() = _ticks.emit(Unit)

    override suspend fun awaitNextTick() {
        _ticks.take(1).collect{}
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAppUsageTrackerTest {
    private lateinit var appUsageDao: AppUsageDao
    private lateinit var appUsageTracker: DefaultAppUsageTracker
    private lateinit var fakeTimeProvider: FakeTimeProvider
    private lateinit var testHeartbeatTicker: TestHeartbeatTicker
    private lateinit var logger: Logger

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        appUsageDao = mockk(relaxed = true)
        fakeTimeProvider = FakeTimeProvider(initialTime = 0L)
        testHeartbeatTicker = TestHeartbeatTicker()
        logger = mockk(relaxed = true)
        appUsageTracker = DefaultAppUsageTracker(
            testScope.backgroundScope,
            appUsageDao,
            fakeTimeProvider,
            testHeartbeatTicker,
            logger
        )
    }

    @Test
    fun `onProfileSwitched inserts switch log with correct profileId and timestamp`() = testScope.runTest {
        val slot = slot<ProfileSwitchLogEntity>()
        coEvery { appUsageDao.insertSwitchLog(capture(slot)) } returns Unit

        appUsageTracker.onProfileSwitched("profile_test", 1024L)
        runCurrent()

        assertEquals("profile_test", slot.captured.profileId)
        assertEquals(1024L, slot.captured.switchedAt)
    }

    @Test
    fun `onBootCompleted closes orphaned session at reboot`() = testScope.runTest {
        appUsageTracker.onBootCompleted(4096L)
        runCurrent()

        coVerify { appUsageDao.closeOrphanedSessions(4096L) }
    }

    @Test
    fun `session below MIN_SESSION_MILLISECONDS is not written to room`() = testScope.runTest {
        appUsageTracker.onAppForegrounded(
            "com.example.app", 0L,
            "profile_test", 0L
        )
        fakeTimeProvider.setTime(UsageConstants.MIN_SESSION_MILLISECONDS - 1)
        testHeartbeatTicker.tick()
        runCurrent()

        appUsageTracker.onAppBackgrounded(
            "com.example.app", 0L,
            UsageConstants.MIN_SESSION_MILLISECONDS - 1
        )
        runCurrent()

        coVerify(exactly = 0) { appUsageDao.insertSession(any()) }
    }

    @Test
    fun `session is written to db after MIN_SESSION_MILLISECONDS elapses`() = testScope.runTest {
        appUsageTracker.onAppForegrounded(
            "com.example.app", 0L,
            "profile_test", 0L
        )
        fakeTimeProvider.setTime(UsageConstants.MIN_SESSION_MILLISECONDS + 64L)
        testHeartbeatTicker.tick()
        runCurrent()

        coVerify(exactly = 1) { appUsageDao.insertSession(any()) }
        appUsageTracker.onAppBackgrounded(
            "com.example.app", 0L,
            UsageConstants.MIN_SESSION_MILLISECONDS + 64
        )
        runCurrent()
    }

    @Test
    fun `session is marked to the profile active at foreground time`() = testScope.runTest {
        val sessionSlot = slot<AppUsageSessionEntity>()
        coEvery { appUsageDao.insertSession(capture(sessionSlot)) } returns Unit

        appUsageTracker.onAppForegrounded(
            "com.example.app", 0L,
            "profile_test", 0L
        )
        fakeTimeProvider.setTime(UsageConstants.MIN_SESSION_MILLISECONDS + 64L)
        testHeartbeatTicker.tick()
        runCurrent()

        assertEquals("profile_test", sessionSlot.captured.profileId)

        appUsageTracker.onAppBackgrounded(
            "com.example.app", 0L,
            UsageConstants.MIN_SESSION_MILLISECONDS - 1
        )
        runCurrent()
    }

    @Test
    fun `backgrounding app writes a session to DB`() = testScope.runTest {
        appUsageTracker.onAppForegrounded(
            "com.example.app", 0L,
            "profile_test", 0L
        )
        fakeTimeProvider.setTime(UsageConstants.MIN_SESSION_MILLISECONDS + 64L)
        testHeartbeatTicker.tick()
        runCurrent()

        appUsageTracker.onAppBackgrounded(
            "com.example.app", 0L, UsageConstants.MIN_SESSION_MILLISECONDS + 128L
        )
        runCurrent()

        coVerify { appUsageDao.updateSessionEndTime(any(), any()) }
    }

    @Test
    fun `backgrounding a short session does not write to DB`() = testScope.runTest {
        appUsageTracker.onAppForegrounded(
            "com.example.app", 0L,
            "profile_test", 0L
        )
        fakeTimeProvider.setTime(UsageConstants.MIN_SESSION_MILLISECONDS - 1L)

        appUsageTracker.onAppBackgrounded(
            "com.example.app", 0L, UsageConstants.MIN_SESSION_MILLISECONDS - 1L
        )
        runCurrent()

        coVerify(exactly = 0) { appUsageDao.insertSession(any()) }
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

        fakeTimeProvider.setTime(UsageConstants.MIN_SESSION_MILLISECONDS + 64L)
        testHeartbeatTicker.tick()
        runCurrent()

        coVerify(exactly = 1) { appUsageDao.insertSession(any()) }
        appUsageTracker.onAppBackgrounded(
            "com.example.app", 1L,
            UsageConstants.MIN_SESSION_MILLISECONDS - 1
        )
        runCurrent()
    }

    @Test
    fun `foregrounding a new app closes the last active app session`() = testScope.runTest {
        appUsageTracker.onAppForegrounded(
            "com.example.app.one", 0L,
            "profile_test", 0L
        )
        fakeTimeProvider.setTime(UsageConstants.MIN_SESSION_MILLISECONDS + 64L)
        testHeartbeatTicker.tick()
        runCurrent()

        coVerify(exactly = 1) { appUsageDao.insertSession(any()) }

        val closeTime = UsageConstants.MIN_SESSION_MILLISECONDS + 128L
        fakeTimeProvider.setTime(closeTime)

        appUsageTracker.onAppForegrounded(
            "com.example.app.two", 0L,
            "profile_test", closeTime
        )
        runCurrent()

        coVerify(exactly = 1) { appUsageDao.updateSessionEndTime(any(), closeTime) }
        appUsageTracker.onAppBackgrounded(
            "com.example.app.two", 0L, closeTime
        )
        runCurrent()
    }
}
