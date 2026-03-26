package com.lumina.domain.coordination.usecase

import com.lumina.core.model.AppBasicData
import com.lumina.core.model.AppCategory
import com.lumina.core.model.AppInfo
import com.lumina.core.model.AppOverrideState
import com.lumina.core.model.componentKey
import com.lumina.domain.apps.InstalledAppsRepository
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.LauncherProfile
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveActiveProfileAppsMappingUseCaseTest {
    private val profileRepository: ProfileRepository = mockk()
    private val installedAppsRepository: InstalledAppsRepository = mockk()

    private val useCase = ObserveActiveProfileAppsMappingUseCase(profileRepository, installedAppsRepository)

    @Test
    fun `invoke combines installed app with profile overrides`() = runTest {
        val profileId = "profile_test"
        val appBasicData = AppBasicData("com.example.app", 0L)

        val profile = mockk<LauncherProfile> { every { id } returns profileId }
        every { profileRepository.activeProfile } returns MutableStateFlow(profile)

        val appInfoMap = mapOf(
            appBasicData.componentKey to AppInfo(
                packageName = appBasicData.packageName,
                userHandleNumber = appBasicData.userHandleNumber,
                componentClassName = "Main",
                displayName = "Example",
                category = AppCategory.ENTERTAINMENT,
            )
        )
        every { installedAppsRepository.appsMap } returns MutableStateFlow(appInfoMap)

        val override = listOf(
            AppOverrideState(
                appBasicData = appBasicData,
                showCountdown = false,
                recommendedUsageMinutes = 0
            )
        )
        every { profileRepository.getAppsForProfile(profileId) } returns MutableStateFlow(override)

        val result = useCase().first()
        assertTrue(result.containsKey(appBasicData.componentKey))
    }
}
