package com.lumina.domain.coordination.usecase

import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.domain.profiles.model.LauncherProfileOverrides
import com.lumina.domain.settings.AppListSettings
import com.lumina.domain.settings.CountdownSettings
import com.lumina.domain.settings.HomeSettings
import com.lumina.domain.settings.LauncherSettings
import com.lumina.domain.settings.LayoutSettings
import com.lumina.domain.settings.SearchSettings
import com.lumina.domain.settings.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetActiveHomeConfigUseCaseTest {
    private val settingsRepository: SettingsRepository = mockk()
    private val profileRepository: ProfileRepository = mockk()

    private val useCase = GetActiveHomeConfigUseCase(settingsRepository, profileRepository)
    private val defaultSettings = LauncherSettings(
        home = HomeSettings(
            showClock = true,
            showBigClock = true,
            showDate = true,
            showScreenTimePage = true,
            showScreenTimeWithAppName = true
        ),
        appList = AppListSettings(),
        search = SearchSettings(),
        countdown = CountdownSettings(),
        layout = LayoutSettings()
    )

    @Test
    fun `returns default settings when no active profile`() = runTest {
        every { settingsRepository.allSettings } returns MutableStateFlow(defaultSettings)
        every { profileRepository.activeProfile } returns MutableStateFlow(null)

        val result = useCase().first()
        assertEquals(defaultSettings.home, result.home)
        assertNull(result.theme.background)
        assertNull(result.theme.font)
    }

    @Test
    fun `returns profile overrides if present`() = runTest {
        val profileOverrides = LauncherProfileOverrides(
            background = "bg",
            font = "font",
            showClock = false,
            showBigClock = false,
            showDate = false,
            showWeather = false,
            hideScreenTime = false
        )
        every { settingsRepository.allSettings } returns MutableStateFlow(defaultSettings)
        every { profileRepository.activeProfile } returns MutableStateFlow(mockk<LauncherProfile> {
            every { overrides } returns profileOverrides
        })

        val result = useCase().first()
        assertFalse(result.home.showDate)
        assertFalse(result.home.showClock)
        assertFalse(result.home.showBigClock)

        assertEquals(profileOverrides.background, result.theme.background)
        assertEquals(profileOverrides.font, result.theme.font)
    }

    @Test
    fun `settings change when profile is changed`() = runTest {
        every { settingsRepository.allSettings } returns MutableStateFlow(defaultSettings)
        every { profileRepository.activeProfile } returns MutableStateFlow(null)
        val result = useCase().first()

        every { settingsRepository.allSettings } returns MutableStateFlow(defaultSettings.copy(
            home = defaultSettings.home.copy(showClock = false)
        ))
        val updated = useCase().first()

        assertTrue(result.home.showClock)
        assertFalse(updated.home.showClock)
    }
}