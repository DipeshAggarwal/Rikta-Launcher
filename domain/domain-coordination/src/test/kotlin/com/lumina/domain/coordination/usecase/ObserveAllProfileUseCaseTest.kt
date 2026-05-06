package com.lumina.domain.coordination.usecase

import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.LauncherProfile
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveAllProfileUseCaseTest {
    private val profileRepository: ProfileRepository = mockk()

    private val useCase = ObserveAllProfileUseCase(profileRepository)

    @Test
    fun `map profiles to id, name pairs`() = runTest {
        val profileOne = mockk<LauncherProfile> {
            every { id } returns "profile_1"
            every { name } returns "Default"
        }
        val profileTwo = mockk<LauncherProfile> {
            every { id } returns "profile_2"
            every { name } returns "Focus"
        }

        every { profileRepository.getAllProfiles() } returns MutableStateFlow(listOf(profileOne, profileTwo))
        val result = useCase().first()

        assertEquals(2, result.size)
        assertEquals(Pair(profileOne.id, profileOne.name), result[0])
        assertEquals(Pair(profileTwo.id, profileTwo.name), result[1])
    }

    @Test
    fun `map is updated when profile list changes`() = runTest {
        val profileFlow = MutableStateFlow<List<LauncherProfile>>(emptyList())
        every { profileRepository.getAllProfiles() } returns profileFlow

        val initialResult = useCase().first()
        assertTrue(initialResult.isEmpty())

        val newProfile = mockk<LauncherProfile> {
            every { id } returns "profile_1"
            every { name } returns "Default"
        }
        profileFlow.value = listOf(newProfile)

        val updatedResult = useCase().first()
        assertEquals(1, updatedResult.size)
        assertEquals(Pair(newProfile.id, newProfile.name), updatedResult.first())
    }
}
