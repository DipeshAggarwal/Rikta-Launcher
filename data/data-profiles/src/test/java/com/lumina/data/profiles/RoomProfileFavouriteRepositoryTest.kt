package com.lumina.data.profiles

import com.lumina.core.database.dao.ProfileFavouriteDao
import com.lumina.core.logging.Logger
import com.lumina.core.testing.builder.ProfileFavouriteEntityBuilder
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RoomProfileFavouriteRepositoryTest {
    private lateinit var profileFavouriteDao: ProfileFavouriteDao
    private lateinit var roomProfileFavouriteRepository: RoomProfileFavouriteRepository
    private lateinit var logger: Logger

    private val gap = 128
    private val profileId = "profile_test_1"
    private val itemId = "com.example.one::0"

    @Before
    fun setup() {
        profileFavouriteDao = mockk(relaxed = true)
        logger = mockk(relaxed = true)
        roomProfileFavouriteRepository = RoomProfileFavouriteRepository(profileFavouriteDao, logger)
    }

    @Test
    fun `getAll returns entities mapped to FavouriteItem`() = runTest {
        val entity = ProfileFavouriteEntityBuilder.build(profileId = profileId, itemId = itemId)
        every { profileFavouriteDao.getAll(profileId) } returns flowOf(listOf(entity))

        val result = roomProfileFavouriteRepository.getAll(profileId).first()
        assertEquals(1, result.size)
        assertEquals(itemId, result.first().itemId)
    }

    @Test
    fun `update calculates correct order when moved between two items`() = runTest {
        val previous = gap * 2
        val next = gap * 3
        val newOrder = (previous + next) / 2

        roomProfileFavouriteRepository.update(profileId, itemId, previous, next)
        coVerify { profileFavouriteDao.update(profileId, itemId, newOrder) }
    }

    @Test
    fun `update calculates correct order when moved to the very top`() = runTest {
        val previous = null
        val next = gap * 2
        val newOrder = next / 2

        roomProfileFavouriteRepository.update(profileId, itemId, previous, next)
        coVerify { profileFavouriteDao.update(profileId, itemId, newOrder) }
    }

    @Test
    fun `update calculates correct order when moved to the very bottom`() = runTest {
        val previous = gap * 3
        val next = null
        val newOrder = previous + gap

        roomProfileFavouriteRepository.update(profileId, itemId, previous, next)
        coVerify { profileFavouriteDao.update(profileId, itemId, newOrder) }
    }

    @Test
    fun `update triggers rebalance when gap closes`() = runTest {
        roomProfileFavouriteRepository.update(profileId, "com.example.one::0", 128, 132)
        coVerify(exactly = 1) { profileFavouriteDao.update(profileId, "com.example.one::0", 130) }
        coVerify(exactly = 0) { profileFavouriteDao.rebalance(any(), any()) }

        roomProfileFavouriteRepository.update(profileId, "com.example.two::0", 128, 130)
        coVerify(exactly = 1) { profileFavouriteDao.update(profileId, "com.example.two::0", 129) }
        coVerify(exactly = 0) { profileFavouriteDao.rebalance(any(), any()) }

        roomProfileFavouriteRepository.update(profileId, "com.example.three::0", 128, 129)
        coVerify(exactly = 1) { profileFavouriteDao.update(profileId, "com.example.three::0", 128) }
        coVerify(exactly = 1) { profileFavouriteDao.rebalance(profileId, gap) }
    }
}
