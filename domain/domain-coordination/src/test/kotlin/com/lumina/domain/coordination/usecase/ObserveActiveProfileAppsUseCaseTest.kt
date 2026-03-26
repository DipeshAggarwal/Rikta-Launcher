package com.lumina.domain.coordination.usecase

import com.lumina.core.model.AppBasicData
import com.lumina.core.model.AppCategory
import com.lumina.core.model.AppInfo
import com.lumina.core.model.LauncherItem
import com.lumina.core.model.SystemProfileIds
import com.lumina.core.model.componentKey
import com.lumina.domain.apps.HiddenAppsRepository
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.LauncherProfile
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveActiveProfileAppsUseCaseTest {
    private val profileRepository: ProfileRepository = mockk()
    private val hiddenAppsRepository: HiddenAppsRepository = mockk()
    private val appsMappingUseCase: ObserveActiveProfileAppsMappingUseCase = mockk()

    private val useCase = ObserveActiveProfileAppsUseCase(
        profileRepository, hiddenAppsRepository, appsMappingUseCase
    )

    @Test
    fun `default profile filters otu hidden apps`() = runTest {
        val appVisibleData = AppBasicData("com.visible.app", 0L)
        val appHiddenData = AppBasicData("com.hidden.app", 0L)

        val defaultProfile = mockk<LauncherProfile> { every { id } returns SystemProfileIds.DEFAULT }
        every { profileRepository.activeProfile } returns MutableStateFlow(defaultProfile)
        every { hiddenAppsRepository.appPackages } returns MutableStateFlow(
            setOf(appHiddenData.packageName)
        )

        val mapping = mapOf(
            appHiddenData.componentKey to LauncherItem.App(
                info = AppInfo(
                    appHiddenData.packageName,
                    componentClassName = "Main",
                    userHandleNumber = appHiddenData.userHandleNumber,
                    displayName = "Hidden",
                    category = AppCategory.ENTERTAINMENT
                ),
                showCountdown = false,
                recommendedUsageMinutes = null,
                favouriteOrder = null
            ),
            appVisibleData.componentKey to LauncherItem.App(
                info = AppInfo(
                    appVisibleData.packageName,
                    componentClassName = "Main",
                    userHandleNumber = appVisibleData.userHandleNumber,
                    displayName = "Visible",
                    category = AppCategory.ENTERTAINMENT
                ),
                showCountdown = false,
                recommendedUsageMinutes = null,
                favouriteOrder = null
            )
        )
        every { appsMappingUseCase() } returns MutableStateFlow(mapping)

        val result = useCase().first()
        assertEquals(1, result.size)
        assertEquals(appVisibleData.packageName, result.first().info.packageName)
    }
}
