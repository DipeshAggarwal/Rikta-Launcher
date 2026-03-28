package com.lumina.data.coordination

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lumina.core.model.AppCategory
import com.lumina.core.model.AppInfo
import com.lumina.core.model.LauncherItem
import com.lumina.domain.coordination.IntentLauncher
import com.lumina.domain.coordination.LaunchState
import com.lumina.domain.coordination.usecase.ObserveActiveProfileCountdownAppsUseCase
import com.lumina.domain.countdown.CountdownAppsRepository
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlatformAppLaunchCoordinatorTest {
    private lateinit var intentLauncher: IntentLauncher
    private lateinit var countdownRepository: CountdownAppsRepository
    private lateinit var observeCountdownApps: ObserveActiveProfileCountdownAppsUseCase

    private lateinit var appLaunchCoordinator: PlatformAppLaunchCoordinator

    private val testApp = AppInfo(
        packageName = "com.example.app",
        componentClassName = "Main",
        userHandleNumber = 0L,
        displayName = "Example App",
        category = AppCategory.ENTERTAINMENT,
        customCategoryName = null
    )

    @Before
    fun setup() {
        intentLauncher = mockk(relaxed = true)
        countdownRepository = mockk(relaxed = true)
        observeCountdownApps = mockk(relaxed = true)

        appLaunchCoordinator = PlatformAppLaunchCoordinator(intentLauncher, countdownRepository, observeCountdownApps)

        coEvery { observeCountdownApps() } returns MutableStateFlow(emptyList())
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun requestLaunch_opens_app_directly_if_no_countdown_needed() = runTest {
        appLaunchCoordinator.requestLaunch(testApp)

        coVerify { intentLauncher.openApp(testApp) }
        assertEquals(LaunchState.Idle, appLaunchCoordinator.launchState.value)
    }

    @Test
    fun requestLaunch_sets_RequiresCountdown_state_if_app_requires_countdown() = runTest {
        coEvery { observeCountdownApps() } returns MutableStateFlow(listOf(
            LauncherItem.App(
                info = testApp,
                showCountdown = true,
                recommendedUsageMinutes = 8,
                favouriteOrder = 1
            )
        ))
        appLaunchCoordinator.requestLaunch(testApp)

        coVerify(exactly = 0) { intentLauncher.openApp(testApp) }
        assertTrue(appLaunchCoordinator.launchState.value is LaunchState.RequiresCountdown)
    }

    @Test
    fun completeLaunch_opens_pending_app_and_resets() = runTest {
        coEvery { observeCountdownApps() } returns MutableStateFlow(listOf(
            LauncherItem.App(
                info = testApp,
                showCountdown = true,
                recommendedUsageMinutes = 8,
                favouriteOrder = 1
            )
        ))

        appLaunchCoordinator.requestLaunch(testApp)
        appLaunchCoordinator.completeLaunch()

        coVerify(exactly = 1) { intentLauncher.openApp(testApp) }
        assertEquals(LaunchState.Idle, appLaunchCoordinator.launchState.value)
    }

    @Test
    fun cancelLaunch_resets_state() = runTest {
        coEvery { observeCountdownApps() } returns MutableStateFlow(listOf(
            LauncherItem.App(
                info = testApp,
                showCountdown = true,
                recommendedUsageMinutes = 8,
                favouriteOrder = 1
            )
        ))

        appLaunchCoordinator.requestLaunch(testApp)
        appLaunchCoordinator.cancelLaunch()

        coVerify(exactly = 0) { intentLauncher.openApp(testApp) }
        assertEquals(LaunchState.Idle, appLaunchCoordinator.launchState.value)
    }
}
