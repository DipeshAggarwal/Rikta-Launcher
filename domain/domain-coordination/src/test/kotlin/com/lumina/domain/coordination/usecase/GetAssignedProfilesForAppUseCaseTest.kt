package com.lumina.domain.coordination.usecase

import com.lumina.domain.profiles.ProfileRepository
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetAssignedProfilesForAppUseCaseTest {
    private val profileRepository: ProfileRepository = mockk()

    private val useCase = GetAssignedProfilesForAppUseCase(profileRepository)

    @Test
    fun `returns expected set of profile ids for app`() = runTest {
        val packageName = "com.example.app"
        val userHandleNumber = 0L
        val expectedProfiles = setOf("profile_1", "profile_2")

        every {
            profileRepository.getProfileIdsForApp(packageName, userHandleNumber)
        } returns MutableStateFlow(expectedProfiles)

        val result = useCase(packageName, userHandleNumber).first()
        assertEquals(2, result.size)
        assertTrue(result.contains(expectedProfiles.elementAt(0)))
        assertTrue(result.contains(expectedProfiles.elementAt(1)))
    }

    @Test
    fun `returns emptySet when no profile associated with app`() = runTest {
        val packageName = "com.example.app"
        val userHandleNumber = 0L

        every {
            profileRepository.getProfileIdsForApp(packageName, userHandleNumber)
        } returns MutableStateFlow(emptySet())

        val result = useCase(packageName, userHandleNumber).first()
        assertTrue(result.isEmpty())
    }
}
