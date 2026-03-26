package com.lumina.domain.coordination.usecase

import com.lumina.core.model.AppBasicData
import com.lumina.core.model.AppCategory
import com.lumina.core.model.AppInfo
import com.lumina.core.model.FavouriteItem
import com.lumina.core.model.FavouriteItemType
import com.lumina.core.model.LauncherItem
import com.lumina.core.model.LauncherShortcut
import com.lumina.core.model.ShortcutType
import com.lumina.core.model.componentKey
import com.lumina.domain.profiles.ProfileFavouriteRepository
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.LauncherProfile
import com.lumina.domain.shortcut.ShortcutRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveActiveProfileFavouriteUseCaseTest {
    private val profileRepository: ProfileRepository = mockk()
    private val profileFavouriteRepository: ProfileFavouriteRepository = mockk()
    private val shortcutRepository: ShortcutRepository = mockk()
    private val appsMappingUseCase: ObserveActiveProfileAppsMappingUseCase = mockk()

    private val useCase = ObserveActiveProfileFavouritesUseCase(
        profileRepository, profileFavouriteRepository, shortcutRepository, appsMappingUseCase
    )

    @Test
    fun `invoke combines and returns sorted favourite app and shortcuts`() = runTest {
        val profileId = "profile_test"
        val appBasicData = AppBasicData("com.example.app", 0L)
        val shortcutData = LauncherShortcut(
            id = "shortcut_1",
            label = "Shortcut 1",
            type = ShortcutType.APP_SHORTCUT,
            shortcutPackage = "com.example.app",
            shortcutId = "shortcut_1",
            url = null,
            targetPackage = null
        )

        val profile = mockk<LauncherProfile> { every { id } returns profileId }
        every { profileRepository.activeProfile } returns MutableStateFlow(profile)

        val mapping = mapOf(
            appBasicData.componentKey to LauncherItem.App(
                info = AppInfo(
                    appBasicData.packageName,
                    componentClassName = "Main",
                    userHandleNumber = appBasicData.userHandleNumber,
                    displayName = "One",
                    category = AppCategory.ENTERTAINMENT
                ),
                showCountdown = true,
                recommendedUsageMinutes = null,
                favouriteOrder = null
            )
        )
        every { appsMappingUseCase() } returns MutableStateFlow(mapping)

        val shortcuts = listOf(shortcutData)
        every { shortcutRepository.get(profileId) } returns MutableStateFlow(shortcuts)

        val favourites = listOf(
            FavouriteItem(appBasicData.componentKey, FavouriteItemType.APP, 128),
            FavouriteItem(shortcutData.id, FavouriteItemType.SHORTCUT, 256),
        )
        every { profileFavouriteRepository.getAll(profileId) } returns MutableStateFlow(favourites)

        val result = useCase().first()
        assertEquals(2, result.size)

        assertTrue(result[0] is LauncherItem.App)
        assertEquals(128, result[0].favouriteOrder)

        assertTrue(result[1] is LauncherItem.Shortcut)
        assertEquals(256, result[1].favouriteOrder)
    }
}
