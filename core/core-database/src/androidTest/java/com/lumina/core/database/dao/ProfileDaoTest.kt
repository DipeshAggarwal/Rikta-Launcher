package com.lumina.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lumina.core.database.LuminaDatabase
import com.lumina.core.database.entity.NotificationWhitelistEntity
import com.lumina.core.database.entity.ProfileAppCrossRef
import com.lumina.core.testing.builder.ProfileEntityBuilder
import com.lumina.core.testing.builder.ProfileTriggerEntityBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileDaoTest {
    private lateinit var database: LuminaDatabase
    private lateinit var profileDao: ProfileDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LuminaDatabase::class.java
        ).build()
        profileDao = database.profileDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveProfile_persists_profile_visible_via_getAllProfiles() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1", name = "Test Profile One")
        profileDao.saveProfile(profile)

        val result = profileDao.getAllProfiles().first()
        assertEquals(1, result.size)
        assertEquals(profile.id, result.first().id)
        assertEquals(profile.name, result.first().name)
    }

    @Test
    fun saveProfile_replaces_existing_profile_with_same_id() = runTest {
        val profileOld = ProfileEntityBuilder.build(id = "profile_test_1", name = "Test Profile One")
        profileDao.saveProfile(profileOld)

        val profileNew = ProfileEntityBuilder.build(id = "profile_test_1", name = "Test Profile New")
        profileDao.saveProfile(profileNew)

        val result = profileDao.getAllProfiles().first()
        assertEquals(1, result.size)
        assertEquals(profileNew.name, result.first().name)
    }

    @Test
    fun getProfileById_returns_null_for_nonexistent_id() = runTest {
        val result = profileDao.getProfileById("not_here").first()
        assertNull(result)
    }

    @Test
    fun getProfileById_returns_correct_profile_by_id() = runTest {
        val profileOne = ProfileEntityBuilder.build(id = "profile_test_1")
        val profileTwo = ProfileEntityBuilder.build(id = "profile_test_2", name = "Test Profile Two")

        profileDao.saveProfile(profileOne)
        profileDao.saveProfile(profileTwo)

        val result = profileDao.getProfileById(profileTwo.id).first()
        assertNotNull(result)
        assertEquals(profileTwo.name, result!!.name)
    }

    @Test
    fun updateProfile_changes_mutable_fields() = runTest {
        val profileOld = ProfileEntityBuilder.build(id = "profile_test_1", name = "Test Profile One", startDnd = false)
        val profileNew = ProfileEntityBuilder.build(id = "profile_test_1", name = "Test Profile Updated", startDnd = true)

        profileDao.saveProfile(profileOld)
        profileDao.saveProfile(profileNew)

        val result = profileDao.getProfileById(profileOld.id).first()
        assertNotNull(result)
        assertEquals(profileNew.name, result!!.name)
        assertTrue(result.startDnd)
    }

    @Test
    fun deleteProfileById_removes_profile_from_getAllProfiles() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")
        profileDao.deleteProfileById(profile.id)

        val result = profileDao.getAllProfiles().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteProfileById_cascades_to_app_mappings() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")
        val mapping = ProfileAppCrossRef(profile.id, "com.example.app", 0L)

        profileDao.saveProfile(profile)
        profileDao.insertAppMapping(mapping)
        profileDao.deleteProfileById(profile.id)

        val result = profileDao.getAppsForProfile(profile.id).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteProfileById_cascades_to_triggers() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")
        val trigger = ProfileTriggerEntityBuilder.build(triggerId = 1L, profileId = profile.id)

        profileDao.saveProfile(profile)
        profileDao.insertTrigger(trigger)
        profileDao.deleteProfileById(profile.id)

        val result = profileDao.getAppsForProfile(profile.id).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteProfileById_cascades_to_notification_whitelist() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")
        val whitelist = NotificationWhitelistEntity(profile.id, "com.example.app", 0L)

        profileDao.saveProfile(profile)
        profileDao.insertNotificationWhitelist(whitelist)
        profileDao.deleteProfileById(profile.id)

        val result = profileDao.getAppsForProfile(profile.id).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun isKeyTaken_returns_false_when_no_profile_has_key() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1", activationKey = null)
        profileDao.saveProfile(profile)

        assertFalse(profileDao.isKeyTaken("abc123", "profile_test_2"))
    }

    @Test
    fun isKeyTaken_returns_true_when_another_profile_has_same_key() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1", activationKey = "abc123")
        profileDao.saveProfile(profile)

        assertTrue(profileDao.isKeyTaken("abc123", "profile_test_2"))
    }

    @Test
    fun isKeyTaken_returns_false_when_only_excluded_profile_has_key() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1", activationKey = "abc123")
        profileDao.saveProfile(profile)

        assertFalse(profileDao.isKeyTaken("abc123", profile.id))
    }

    @Test
    fun getProfileByActivationKey_returns_matching_profile() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1", activationKey = "abc123")
        profileDao.saveProfile(profile)

        val result = profileDao.getProfileByActivationKey("abc123")
        assertNotNull(result)
        assertEquals(profile.id, result!!.id)
    }

    @Test
    fun getProfileByActivationKey_returns_null_for_wrong_key() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1", activationKey = "abc123")
        profileDao.saveProfile(profile)

        assertNull(profileDao.getProfileByActivationKey("not_abc123"))
    }

    @Test
    fun insertAppMapping_is_returned_by_getAppsForProfile() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")
        val mapping = ProfileAppCrossRef(profile.id, "com.example.app", 0L)

        profileDao.saveProfile(profile)
        profileDao.insertAppMapping(mapping)

        val result = profileDao.getAppsForProfile(profile.id).first()
        assertEquals(1, result.size)
        assertEquals(mapping.packageName, result.first().packageName)
    }

    @Test
    fun deleteAppMapping_removes_only_specified_entry() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")
        val mappingOne = ProfileAppCrossRef(profile.id, "com.example.app.one", 0L)
        val mappingTwo = ProfileAppCrossRef(profile.id, "com.example.app.two", 0L)

        profileDao.saveProfile(profile)
        profileDao.insertAppMapping(mappingOne)
        profileDao.insertAppMapping(mappingTwo)

        profileDao.deleteAppMapping(profile.id, mappingOne.packageName, mappingOne.userHandleNumber)
        val result = profileDao.getAppsForProfile(profile.id).first()

        assertEquals(1, result.size)
        assertEquals(mappingTwo.packageName, result.first().packageName)
    }

    @Test
    fun getAppLimitMinutes_returns_null_when_not_set() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")
        val mapping = ProfileAppCrossRef(profile.id, "com.example.app", 0L)

        profileDao.saveProfile(profile)
        profileDao.insertAppMapping(mapping)

        assertNull(profileDao.getAppLimitMinutes(profile.id, mapping.packageName, mapping.userHandleNumber))
    }

    @Test
    fun getAppLimitMinutes_returns_stored_value() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")
        val mapping = ProfileAppCrossRef(
            profile.id, "com.example.app", 0L,
            recommendedUsageMinutes = 64
        )

        profileDao.saveProfile(profile)
        profileDao.insertAppMapping(mapping)

        assertEquals(64, profileDao.getAppLimitMinutes(
            profile.id, mapping.packageName, mapping.userHandleNumber
        ))
    }

    @Test
    fun insertTriggerWithOrder_assigns_zero_based_sequential_order() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")

        val triggerOne = ProfileTriggerEntityBuilder.build(triggerId = 1L, profileId = profile.id)
        val triggerTwo = ProfileTriggerEntityBuilder.build(triggerId = 2L, profileId = profile.id)
        val triggerThree = ProfileTriggerEntityBuilder.build(triggerId = 3L, profileId = profile.id)

        profileDao.saveProfile(profile)
        profileDao.insertTriggerWithOrder(triggerOne)
        profileDao.insertTriggerWithOrder(triggerTwo)
        profileDao.insertTriggerWithOrder(triggerThree)

        val triggers = profileDao.getTriggersForProfile(profile.id).first()
        assertEquals(3, triggers.size)
        assertEquals(0, triggers[0].sequenceOrder)
        assertEquals(1, triggers[1].sequenceOrder)
        assertEquals(2, triggers[2].sequenceOrder)
    }

    @Test
    fun insertTriggerWithOrder_sequences_independently_per_profile() = runTest {
        val profileOne = ProfileEntityBuilder.build(id = "profile_test_1")
        val profileTwo = ProfileEntityBuilder.build(id = "profile_test_2")

        val triggerOne = ProfileTriggerEntityBuilder.build(triggerId = 1L, profileId = profileOne.id)
        val triggerTwo = ProfileTriggerEntityBuilder.build(triggerId = 2L, profileId = profileOne.id)
        val triggerThree = ProfileTriggerEntityBuilder.build(triggerId = 3L, profileId = profileTwo.id)

        profileDao.saveProfile(profileOne)
        profileDao.saveProfile(profileTwo)

        profileDao.insertTriggerWithOrder(triggerOne)
        profileDao.insertTriggerWithOrder(triggerTwo)
        profileDao.insertTriggerWithOrder(triggerThree)

        val result = profileDao.getTriggersForProfile(profileTwo.id).first()
        assertEquals(0, result.first().sequenceOrder)
    }

    @Test
    fun getTriggersForProfile_returns_triggers_sorted_by_sequenceOrder() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")

        val triggerOne = ProfileTriggerEntityBuilder.build(triggerId = 1L, profileId = profile.id)
        val triggerTwo = ProfileTriggerEntityBuilder.build(triggerId = 2L, profileId = profile.id)
        val triggerThree = ProfileTriggerEntityBuilder.build(triggerId = 3L, profileId = profile.id)

        profileDao.saveProfile(profile)
        profileDao.insertTriggerWithOrder(triggerThree)
        profileDao.insertTriggerWithOrder(triggerOne)
        profileDao.insertTriggerWithOrder(triggerTwo)

        val triggers = profileDao.getTriggersForProfile(profile.id).first()
        assertEquals(3, triggers.size)
        assertEquals(0, triggers[0].sequenceOrder)
        assertEquals(1, triggers[1].sequenceOrder)
        assertEquals(2, triggers[2].sequenceOrder)
    }

    @Test
    fun deleteTrigger_removes_only_specified_trigger_by_id() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")

        val triggerOne = ProfileTriggerEntityBuilder.build(triggerId = 1L, profileId = profile.id)
        val triggerTwo = ProfileTriggerEntityBuilder.build(triggerId = 2L, profileId = profile.id)

        profileDao.saveProfile(profile)
        profileDao.insertTriggerWithOrder(triggerOne)
        profileDao.insertTriggerWithOrder(triggerTwo)

        profileDao.deleteTrigger(triggerOne.triggerId)
        val triggers = profileDao.getTriggersForProfile(profile.id).first()

        assertEquals(1, triggers.size)
        assertEquals(2L, triggers.first().triggerId)
    }

    @Test
    fun clearTriggers_removes_all_triggers_for_profile() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")

        val triggerOne = ProfileTriggerEntityBuilder.build(triggerId = 1L, profileId = profile.id)
        val triggerTwo = ProfileTriggerEntityBuilder.build(triggerId = 2L, profileId = profile.id)

        profileDao.saveProfile(profile)
        profileDao.insertTriggerWithOrder(triggerOne)
        profileDao.insertTriggerWithOrder(triggerTwo)

        profileDao.clearTriggers(profile.id)
        assertTrue(profileDao.getTriggersForProfile(profile.id).first().isEmpty())
    }

    @Test
    fun getTriggerCount_returns_zero_for_profile_with_no_triggers() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")
        profileDao.saveProfile(profile)

        assertEquals(0, profileDao.getTriggerCount(profile.id))
    }

    @Test
    fun getTriggerCount_returns_correct_count_after_inserts() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")

        val triggerOne = ProfileTriggerEntityBuilder.build(triggerId = 1L, profileId = profile.id)
        val triggerTwo = ProfileTriggerEntityBuilder.build(triggerId = 2L, profileId = profile.id)

        profileDao.saveProfile(profile)
        profileDao.insertTriggerWithOrder(triggerOne)
        profileDao.insertTriggerWithOrder(triggerTwo)

        assertEquals(2, profileDao.getTriggerCount(profile.id))
    }

    @Test
    fun insertWhitelist_is_returned_by_getNotificationNotificationWhitelist() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")
        val whitelist = NotificationWhitelistEntity(profile.id, "com.example.app", 0L)

        profileDao.saveProfile(profile)
        profileDao.insertNotificationWhitelist(whitelist)

        val result = profileDao.getNotificationWhitelist(profile.id).first()
        assertEquals(1, result.size)
        assertEquals(whitelist.packageName, result.first().packageName)
    }

    @Test
    fun deleteWhitelist_removes_specific_entry() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")
        val whitelist = NotificationWhitelistEntity(profile.id, "com.example.app", 0L)

        profileDao.saveProfile(profile)
        profileDao.insertNotificationWhitelist(whitelist)
        profileDao.deleteNotificationWhitelist(whitelist)

        assertTrue(profileDao.getNotificationWhitelist(profile.id).first().isEmpty())
    }
}
