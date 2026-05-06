package com.lumina.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lumina.core.database.LuminaDatabase
import com.lumina.core.testing.builder.ProfileEntityBuilder
import com.lumina.core.testing.builder.ProfileFavouriteEntityBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileFavouriteDaoTest {
    private lateinit var database: LuminaDatabase
    private lateinit var profileDao: ProfileDao
    private lateinit var profileFavouriteDao: ProfileFavouriteDao

    private val gap = 128
    private val profileId = "profile_test_1"

    @Before
    fun setup() = runTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LuminaDatabase::class.java
        ).build()
        profileDao = database.profileDao()
        profileFavouriteDao = database.profileFavouriteDao()

        profileDao.saveProfile(ProfileEntityBuilder.build(id = profileId))
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun save_and_get_retrieves_correct_entity() = runTest {
        val entity = ProfileFavouriteEntityBuilder.build(profileId)
        profileFavouriteDao.save(entity)

        val result = profileFavouriteDao.get(profileId, entity.itemId)
        assertNotNull(result)
        assertEquals(entity.favouriteOrder, result!!.favouriteOrder)
    }

    @Test
    fun getMaxOrder_returns_highest_value_or_null() = runTest {
        assertNull(profileFavouriteDao.getMaxOrder(profileId))

        val entityOne = ProfileFavouriteEntityBuilder.build(profileId, "com.example.one::0", favouriteOrder = gap)
        val entityTwo = ProfileFavouriteEntityBuilder.build(profileId, "com.example.two::0", favouriteOrder = 256)

        profileFavouriteDao.save(entityOne)
        profileFavouriteDao.save(entityTwo)

        assertEquals(entityTwo.favouriteOrder, profileFavouriteDao.getMaxOrder(profileId))
    }

    @Test
    fun delete_removes_specified_favourites() = runTest {
        val entity = ProfileFavouriteEntityBuilder.build(profileId)
        profileFavouriteDao.save(entity)

        profileFavouriteDao.delete(profileId, entity.itemId)
        val result = profileFavouriteDao.get(profileId, entity.itemId)
        assertNull(result)
    }

    @Test
    fun update_changes_favourite_order_for_provided_item() = runTest {
        val entity = ProfileFavouriteEntityBuilder.build(profileId)
        profileFavouriteDao.save(entity)

        profileFavouriteDao.update(profileId, entity.itemId, 512)
        val result = profileFavouriteDao.get(profileId, entity.itemId)
        assertNotNull(result)
        assertEquals(512, result!!.favouriteOrder)
    }

    @Test
    fun toggle_inserts_new_item_if_it_does_not_exist() = runTest {
        val entity = ProfileFavouriteEntityBuilder.build(profileId)
        profileFavouriteDao.toggle(profileId, entity.itemId, entity.itemType, gap)

        val result = profileFavouriteDao.get(profileId, entity.itemId)
        assertEquals(gap, result!!.favouriteOrder)
    }

    @Test
    fun toggle_removes_item_if_it_exists() = runTest {
        val entity = ProfileFavouriteEntityBuilder.build(profileId)

        profileFavouriteDao.save(entity)
        profileFavouriteDao.toggle(profileId, entity.itemId, entity.itemType, gap)

        val result = profileFavouriteDao.get(profileId, entity.itemId)
        assertNull(result)
    }

    @Test
    fun rebalance_spaces_orders_evenly() = runTest {
        val entityOne = ProfileFavouriteEntityBuilder.build(profileId, "com.example.one::0", favouriteOrder = 129)
        val entityTwo = ProfileFavouriteEntityBuilder.build(profileId, "com.example.two::0", favouriteOrder = 270)
        val entityThree = ProfileFavouriteEntityBuilder.build(profileId, "com.example.three::0", favouriteOrder = 370)
        val entityFour = ProfileFavouriteEntityBuilder.build(profileId, "com.example.fourth::0", favouriteOrder = 470)

        profileFavouriteDao.save(entityOne)
        profileFavouriteDao.save(entityTwo)
        profileFavouriteDao.save(entityThree)
        profileFavouriteDao.save(entityFour)
        profileFavouriteDao.rebalance(profileId, gap)

        val result = profileFavouriteDao.getAll(profileId).first()
        assertEquals(gap, result[0].favouriteOrder)
        assertEquals(gap * 2, result[1].favouriteOrder)
        assertEquals(gap * 3, result[2].favouriteOrder)
        assertEquals(gap * 4, result[3].favouriteOrder)
    }
}
