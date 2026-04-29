package com.lumina.data.coordination

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.UserHandle
import android.os.UserManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lumina.core.logging.Logger
import com.lumina.core.model.AppBasicData
import com.lumina.core.model.AppCategory
import com.lumina.core.model.AppInfo
import com.lumina.core.model.AppShortcut
import com.lumina.core.model.LauncherItem
import com.lumina.core.model.ShortcutType
import com.lumina.domain.coordination.LaunchResult
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlatformIntentLauncherTest {
    private lateinit var context: Context
    private lateinit var userManager: UserManager
    private lateinit var logger: Logger
    private lateinit var launcherApps: LauncherApps
    private lateinit var packageManager: PackageManager
    private lateinit var testUserHandle: UserHandle

    private lateinit var intentLauncher: PlatformIntentLauncher

    private val testApp = AppInfo(
        packageName = "com.example.app",
        componentClassName = "Main",
        userHandleNumber = 0L,
        displayName = "Example App",
        category = AppCategory.ENTERTAINMENT,
        customCategoryName = null
    )
    private val testShortcut = AppShortcut(
        shortcutId = "shortcut_id",
        app = AppBasicData(testApp.packageName, testApp.userHandleNumber),
        shortLabel = "Test Shortcut",
        longLabel = "Long Test Shortcut",
        rank = 1
    )

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        userManager = mockk(relaxed = true)
        logger = mockk(relaxed = true)

        launcherApps = mockk(relaxed = true)
        packageManager = mockk(relaxed = true)
        testUserHandle = mockk(relaxed = true)

        every { context.packageManager } returns packageManager
        every { userManager.getUserForSerialNumber(0L) } returns testUserHandle

        intentLauncher = PlatformIntentLauncher(context, launcherApps, userManager, logger)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun openApp_launches_main_activity_via_launcherApps() = runTest {
        val result = intentLauncher.openApp(testApp)
        assertTrue(result is LaunchResult.Success)

        val slot = slot<ComponentName>()
        verify(exactly = 1) {
            launcherApps.startMainActivity(
                capture(slot),
                testUserHandle,
                any(),
                any()
            )
        }
        assertEquals(testApp.packageName, slot.captured.packageName)
        assertEquals(testApp.componentClassName, slot.captured.className)
    }

    @Test
    fun openApp_returns_NoLaunchIntent_if_userHandle_is_null() = runTest {
        every { userManager.getUserForSerialNumber(0L) } returns null

        val result = intentLauncher.openApp(testApp)
        assertTrue(result is LaunchResult.NoLaunchIntent)
        verify(exactly = 0) { launcherApps.startMainActivity(any(), any(), any(), any()) }

    }

    @Test
    fun openApp_falls_back_to_PackageManager_on_SecurityException() = runTest {
        every {
            launcherApps.startMainActivity(any(), any(), any(), any())
        } throws SecurityException("Not allowed in this test.")

        val pmIntent = mockk<Intent>(relaxed = true)
        every { packageManager.getLaunchIntentForPackage(testApp.packageName) } returns pmIntent

        val result = intentLauncher.openApp(testApp)
        assertTrue(result is LaunchResult.Success)

        verify { pmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        verify { context.startActivity(pmIntent) }
    }

    @Test
    fun openApp_returns_NoLaunchIntent_if_PackageManager_returns_null() = runTest {
        every {
            launcherApps.startMainActivity(any(), any(), any(), any())
        } throws SecurityException("Not allowed in this test.")
        every { packageManager.getLaunchIntentForPackage(testApp.packageName) } returns null

        val result = intentLauncher.openApp(testApp)
        assertTrue(result is LaunchResult.NoLaunchIntent)
        verify(exactly = 0) { context.startActivity(any()) }
    }

    @Test
    fun uninstallApp_fires_ACTION_DELETE_intent() = runTest {
        val result = intentLauncher.uninstallApp(testApp)
        assertTrue(result is LaunchResult.Success)

        val intentSlot = slot<Intent>()
        verify { context.startActivity(capture(intentSlot)) }

        val capturedIntent = intentSlot.captured
        assertEquals(Intent.ACTION_DELETE, capturedIntent.action)
        assertEquals("package:${testApp.packageName}", capturedIntent.dataString)
        assertTrue((capturedIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    }

    @Test
    fun launchShortcut_starts_shortcut_on_success() = runTest {
        val result = intentLauncher.launchShortcut(testShortcut)
        assertTrue(result is LaunchResult.Success)

        verify {
            launcherApps.startShortcut(
                testShortcut.app.packageName,
                testShortcut.shortcutId,
                null,
                null,
                testUserHandle
            )
        }
    }

    @Test
    fun launchProfileShortcut_with_package_and_id_starts_shortcut() = runTest {
        val databaseShortcut = LauncherItem.Shortcut(
            id = "shortcut_1",
            label = "Shortcut id",
            type = ShortcutType.APP_SHORTCUT,
            userHandleNumber = 0L,
            shortcutPackage = "com.example.app",
            shortcutId = "shortcut_id",
            url = null,
            targetPackage = null,
            favouriteOrder = 2
        )
        val result = intentLauncher.launchProfileShortcut(databaseShortcut)

        assertTrue(result is LaunchResult.Success)
        verify {
            launcherApps.startShortcut(
                databaseShortcut.shortcutPackage!!,
                databaseShortcut.shortcutId!!,
                null,
                null,
                any()

            )
        }
    }

    @Test
    fun launchProfileShortcut_with_url_starts_shortcut() = runTest {
        val databaseShortcut = LauncherItem.Shortcut(
            id = "shortcut_1",
            label = "Shortcut id",
            type = ShortcutType.APP_SHORTCUT,
            userHandleNumber = 0L,
            shortcutPackage = null,
            shortcutId = null,
            url = "https://example.com",
            targetPackage = "com.example.app",
            favouriteOrder = 2
        )
        val result = intentLauncher.launchProfileShortcut(databaseShortcut)
        assertTrue(result is LaunchResult.Success)

        val intentSlot = slot<Intent>()
        verify { context.startActivity(capture(intentSlot)) }
        assertEquals(databaseShortcut.url, intentSlot.captured.dataString)
    }
}
