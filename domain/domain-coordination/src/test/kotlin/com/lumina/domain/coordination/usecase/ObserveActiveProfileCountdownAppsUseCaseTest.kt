package com.lumina.domain.coordination.usecase

import com.lumina.core.model.AppBasicData
import com.lumina.core.model.AppCategory
import com.lumina.core.model.AppInfo
import com.lumina.core.model.LauncherItem
import com.lumina.core.model.componentKey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveActiveProfileCountdownAppsUseCaseTest {
    private val appsMappingUseCase: ObserveActiveProfileAppsMappingUseCase = mockk()
    private val useCase = ObserveActiveProfileCountdownAppsUseCase(appsMappingUseCase)

    @Test
    fun `invoke only returns apps with showCountdown true`() = runTest {
        val appOneData = AppBasicData("com.example.app.one", 0L)
        val appTwoData = AppBasicData("com.example.app.two", 0L)

        val mapping = mapOf(
            appOneData.componentKey to LauncherItem.App(
                info = AppInfo(
                    appOneData.packageName,
                    componentClassName = "Main",
                    userHandleNumber = appOneData.userHandleNumber,
                    originalName = "One",
                    displayName = "One",
                    category = AppCategory.ENTERTAINMENT
                ),
                showCountdown = true,
                recommendedUsageMinutes = null,
                favouriteOrder = null
            ),
            appTwoData.componentKey to LauncherItem.App(
                info = AppInfo(
                    appTwoData.packageName,
                    componentClassName = "Main",
                    userHandleNumber = appTwoData.userHandleNumber,
                    originalName = "Two",
                    displayName = "Two",
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
        assertEquals(appOneData.packageName, result.first().info.packageName)
    }
}