package com.lumina.data.apps.installed

import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.UserHandle
import android.os.UserManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lumina.core.database.dao.AppOverrideDao
import com.lumina.core.database.entity.AppOverrideEntity
import com.lumina.core.logging.Logger
import com.lumina.core.model.AppCategory
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PackageManagerInstalledAppsRepositoryTest {
    private lateinit var context: Context
    private lateinit var installedAppsMonitor: InstalledAppsMonitor
    private lateinit var appOverrideDao: AppOverrideDao
    private lateinit var launcherApps: LauncherApps
    private lateinit var userManager: UserManager
    private lateinit var logger: Logger

    private lateinit var pmInstalledAppsRepository: PackageManagerInstalledAppsRepository

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val monitorFlow = MutableSharedFlow<AppChangeEvent>()
    private val overrideFlow = MutableStateFlow<List<AppOverrideEntity>>(emptyList())

    private val testUserHandle: UserHandle = mockk(relaxed = true)
    private val testUserId = 0L
    private val testPackageName = "com.example.app"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true)
        installedAppsMonitor = mockk(relaxed = true)
        appOverrideDao = mockk(relaxed = true)
        launcherApps = mockk(relaxed = true)
        userManager = mockk(relaxed = true)
        logger = mockk(relaxed = true)

        every { context.packageName } returns testPackageName

        every { userManager.userProfiles } returns listOf(testUserHandle)
        every { userManager.getSerialNumberForUser(testUserHandle) } returns testUserId

        every { installedAppsMonitor.appChanges() } returns monitorFlow
        every { appOverrideDao.getAll() } returns overrideFlow
    }

    fun testSystemApp(
        packageName: String,
        className: String,
        label: String,
        category: Int = ApplicationInfo.CATEGORY_IMAGE
    ): LauncherActivityInfo {
        val testInfo = mockk<LauncherActivityInfo>(relaxed = true)
        val testAppInfo = ApplicationInfo().apply {
            this.packageName = packageName
            this.category = category
        }
        val testComponentName = ComponentName(packageName, className)

        every { testInfo.applicationInfo } returns testAppInfo
        every { testInfo.componentName } returns testComponentName
        every { testInfo.label } returns label

        return testInfo
    }

    @Test
    fun initial_load_fetches_apps_from_LauncherApps() = testScope.runTest {
        val testAppOne = testSystemApp("com.example.one", "Main", "Example")
        val testAppTwo = testSystemApp("com.example.two", "Main", "Example 2")
        every { launcherApps.getActivityList(null, testUserHandle) } returns listOf(testAppOne, testAppTwo)

        pmInstalledAppsRepository = PackageManagerInstalledAppsRepository(
            context, testScope.backgroundScope, testDispatcher, installedAppsMonitor,
            appOverrideDao, launcherApps, userManager, logger
        )
        monitorFlow.emit(AppChangeEvent.Initial)
        advanceUntilIdle()

        val appList = pmInstalledAppsRepository.apps.value
        assertEquals(2, appList.size)

        assertEquals("Example", appList[0].displayName)
        assertEquals("Example 2", appList[1].displayName)
    }

    @Test
    fun database_overrides_are_applied_to_apps() = testScope.runTest {
        val testApp = testSystemApp("com.example.one", "Main", "Example")
        every { launcherApps.getActivityList(null, testUserHandle) } returns listOf(testApp)

        pmInstalledAppsRepository = PackageManagerInstalledAppsRepository(
            context, testScope.backgroundScope, testDispatcher, installedAppsMonitor,
            appOverrideDao, launcherApps, userManager, logger
        )

        val overrideData = AppOverrideEntity(
            packageName = "com.example.one",
            userHandleNumber = testUserId,
            customDisplayName = "Custom App",
            categoryOverride = AppCategory.CUSTOM,
            customCategoryName = "TestApps"
        )
        overrideFlow.value = listOf(overrideData)

        monitorFlow.emit(AppChangeEvent.Initial)
        advanceUntilIdle()

        val result = pmInstalledAppsRepository.apps.value.first()
        assertEquals(overrideData.customDisplayName, result.displayName)
        assertEquals(overrideData.categoryOverride, result.category)
        assertEquals(overrideData.customCategoryName, result.customCategoryName)
    }

    @Test
    fun event_PackageAdded_adds_app_to_apps_flow() = testScope.runTest {
        every { launcherApps.getActivityList(null, testUserHandle) } returns emptyList()
        monitorFlow.emit(AppChangeEvent.Initial)

        val testApp = testSystemApp("com.example.one", "Main", "Example")
        every { launcherApps.getActivityList("com.example.one", testUserHandle) } returns listOf(testApp)

        pmInstalledAppsRepository = PackageManagerInstalledAppsRepository(
            context, testScope.backgroundScope, testDispatcher, installedAppsMonitor,
            appOverrideDao, launcherApps, userManager, logger
        )

        monitorFlow.emit(AppChangeEvent.PackageAdded("com.example.one", testUserHandle))
        advanceUntilIdle()

        val apps = pmInstalledAppsRepository.apps.value
        assertEquals(1, apps.size)
        assertEquals("com.example.one", apps[0].packageName)
    }

    @Test
    fun event_PackageRemoved_removes_app_from_apps_flow() = testScope.runTest {
        val testApp = testSystemApp("com.example.one", "Main", "Example")
        every { launcherApps.getActivityList(null, testUserHandle) } returns listOf(testApp)

        pmInstalledAppsRepository = PackageManagerInstalledAppsRepository(
            context, testScope.backgroundScope, testDispatcher, installedAppsMonitor,
            appOverrideDao, launcherApps, userManager, logger
        )

        monitorFlow.emit(AppChangeEvent.Initial)
        advanceUntilIdle()
        assertEquals(1, pmInstalledAppsRepository.apps.value.size)

        monitorFlow.emit(AppChangeEvent.PackageRemoved("com.example.one", testUserHandle))
        advanceUntilIdle()

        assertTrue(pmInstalledAppsRepository.apps.value.isEmpty())
        coVerify(exactly = 1) { appOverrideDao.delete("com.example.one", testUserId) }
    }

    @Test
    fun getLabel_gets_cached_name_if_available() = testScope.runTest {
        val testApp = testSystemApp("com.example.one", "Main", "Example")
        every { launcherApps.getActivityList(null, testUserHandle) } returns listOf(testApp)

        pmInstalledAppsRepository = PackageManagerInstalledAppsRepository(
            context, testScope.backgroundScope, testDispatcher, installedAppsMonitor,
            appOverrideDao, launcherApps, userManager, logger
        )
        advanceUntilIdle()

        val result = pmInstalledAppsRepository.getLabel("com.example.one")
        assertEquals("Example", result)

        verify(exactly = 1) { launcherApps.getActivityList(null, testUserHandle) }
    }

    @Test
    fun getLabel_returns_from_system_label_if_not_in_cache() = testScope.runTest {
        every { launcherApps.getActivityList(null, testUserHandle) } returns emptyList()

        pmInstalledAppsRepository = PackageManagerInstalledAppsRepository(
            context, testScope.backgroundScope, testDispatcher, installedAppsMonitor,
            appOverrideDao, launcherApps, userManager, logger
        )
        monitorFlow.emit(AppChangeEvent.Initial)
        advanceUntilIdle()

        val testApp = testSystemApp("com.example.one", "Main", "Example")
        every { launcherApps.getActivityList("com.example.one", testUserHandle) } returns listOf(testApp)

        val result = pmInstalledAppsRepository.getLabel("com.example.one")
        assertEquals("Example", result)
    }
}
