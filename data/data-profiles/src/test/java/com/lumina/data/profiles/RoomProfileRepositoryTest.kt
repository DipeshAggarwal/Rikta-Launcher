package com.lumina.data.profiles

import com.lumina.core.database.LuminaDatabase
import com.lumina.core.database.dao.ProfileDao
import com.lumina.core.database.entity.NotificationWhitelistEntity
import com.lumina.core.database.entity.ProfileAppCrossRef
import com.lumina.core.logging.Logger
import com.lumina.core.model.AppBasicData
import com.lumina.core.testing.builder.LauncherProfileBuilder
import com.lumina.core.testing.builder.ProfileEntityBuilder
import com.lumina.core.testing.builder.ProfileTriggerEntityBuilder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.security.MessageDigest

// Relaxed mock of ProfileDataStore returns an empty flow by default.
// And this gets stored in activeProfile. Because of this, stub in test fun are too late.
class FakeProfileDataStore : ProfileDataStore {
    private val _activeProfileId = MutableStateFlow<String?>(null)
    override val activeProfileId: Flow<String?> = _activeProfileId

    override suspend fun setActiveProfileId(id: String?) {
        _activeProfileId.value = id
    }
}

class RoomProfileRepositoryTest {
    private lateinit var database: LuminaDatabase
    private lateinit var profileDao: ProfileDao
    private lateinit var fakeProfileDataStore: FakeProfileDataStore
    private lateinit var roomProfileRepository: RoomProfileRepository
    private lateinit var logger: Logger

    private fun String.mockHash(): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @Before
    fun setup() {
        database = mockk(relaxed = true)
        profileDao = mockk(relaxed = true)
        fakeProfileDataStore = FakeProfileDataStore()
        logger = mockk(relaxed = true)
        roomProfileRepository = RoomProfileRepository(database, profileDao, fakeProfileDataStore, logger)
    }

    @Test
    fun `activeProfile returns null when no active profile`() = runTest {
        fakeProfileDataStore.setActiveProfileId(null)
        assertNull(roomProfileRepository.activeProfile.first())
    }

    @Test
    fun `activeProfile returns null when no active profile in DAO`() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test", name = "Test Profile")
        fakeProfileDataStore.setActiveProfileId("missing")

        every { profileDao.getProfileById("missing") } returns flowOf(null)
        assertNull(roomProfileRepository.activeProfile.first())
    }

    @Test
    fun `activeProfile returns model when profile is found in DAO`() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test", name = "Test Profile")

        fakeProfileDataStore.setActiveProfileId(profile.id)
        every { profileDao.getProfileById(profile.id) } returns flowOf(profile)

        val result = roomProfileRepository.activeProfile.first()
        assertEquals(profile.id, result!!.id)
        assertEquals(profile.name, result.name)
    }

    @Test
    fun `setActiveProfile sets id to profileDataStore`() = runTest {
        roomProfileRepository.setActiveProfile("profile_test")
        assertEquals("profile_test", fakeProfileDataStore.activeProfileId.first())
    }

    @Test
    fun `clearActiveProfile sets null to profileDataStore`() = runTest {
        roomProfileRepository.clearActiveProfile()
        assertNull(fakeProfileDataStore.activeProfileId.first())
    }

    @Test
    fun `getAllProfiles returns entities mapped to LauncherProfile`() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test", name = "Test Profile")
        coEvery { profileDao.getAllProfiles() } returns flowOf(listOf(profile))

        val result = roomProfileRepository.getAllProfiles().first()
        assertEquals(1, result.size)
        assertEquals(profile.id, result.first().id)
        assertEquals(profile.name, result.first().name)
    }

    @Test
    fun `getProfileById returns entity mapped to LauncherProfile or null`() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test", name = "Test Profile")
        coEvery { profileDao.getProfileById(profile.id) } returns flowOf(profile)

        val result = roomProfileRepository.getProfileById(profile.id).first()
        assertEquals(profile.id, result!!.id)

        coEvery { profileDao.getProfileById(profile.id) } returns flowOf(null)
        val nullResult = roomProfileRepository.getProfileById(profile.id).first()
        assertNull(nullResult)
    }

    @Test
    fun `saveProfile throws error when activation key already taken by another profile`() = runTest {
        val profile = LauncherProfileBuilder.build(id = "profile_test", activationKey = "abc123")
        coEvery { profileDao.isKeyTaken(any(), profile.id) } returns true

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { roomProfileRepository.saveProfile(profile) }
        }
        coVerify(exactly = 0) { profileDao.saveProfile(any()) }
    }

    @Test
    fun `saveProfile inserts entity when activation key is unique`() = runTest {
        val profile = LauncherProfileBuilder.build(id = "profile_test", activationKey = "abc123")
        coEvery { profileDao.isKeyTaken(any(), any()) } returns false

        roomProfileRepository.saveProfile(profile)
        coVerify(exactly = 1) { profileDao.saveProfile(any()) }
    }

    @Test
    fun `updateProfile throws when updated key conflicts with another profile`() = runTest {
        val profile = LauncherProfileBuilder.build(id = "profile_test", activationKey = "abc123")
        coEvery { profileDao.isKeyTaken(any(), profile.id) } returns true

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { roomProfileRepository.updateProfile(profile) }
        }
        coVerify(exactly = 0) { profileDao.updateProfile(any()) }
    }

    @Test
    fun `updateProfile delegates to DAO when key is unique`() = runTest {
        val profile = LauncherProfileBuilder.build(id = "profile_test", activationKey = "abc123")
        coEvery { profileDao.isKeyTaken(any(), any()) } returns false

        roomProfileRepository.updateProfile(profile)
        coVerify(exactly = 1) { profileDao.updateProfile(any()) }
    }

    @Test
    fun `deleteProfile delegates profileId to DAO`() = runTest {
        roomProfileRepository.deleteProfile("profile_test")
        coVerify { profileDao.deleteProfileById("profile_test") }
    }

    @Test
    fun `isActivationKeyUnique returns true when key not taken`() = runTest {
        coEvery { profileDao.isKeyTaken(any(), any()) } returns false
        assertTrue(roomProfileRepository.isActivationKeyUnique("abc123", "profile_test"))
    }

    @Test
    fun `isActivationKeyUnique returns false when key already taken`() = runTest {
        coEvery { profileDao.isKeyTaken(any(), any()) } returns true
        assertFalse(roomProfileRepository.isActivationKeyUnique("abc123", "profile_test"))
    }

    @Test
    fun `findProfileByKey returns null for blank key without querying dao`() = runTest {
        assertNull(roomProfileRepository.findProfileByKey(" "))
        coVerify(exactly = 0) { profileDao.getProfileByActivationKey(any()) }
    }

    @Test
    fun `findProfileByKey returns null when no match is found`() = runTest {
        coEvery { profileDao.getProfileByActivationKey(any()) } returns null
        assertNull(roomProfileRepository.findProfileByKey("abc123"))
    }

    @Test
    fun `findProfileByKey returns profile when found`() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test", name = "Test Profile")
        coEvery { profileDao.getProfileByActivationKey(any()) } returns profile

        val result = roomProfileRepository.findProfileByKey("abc123")
        assertEquals(profile.id, result!!.id)
        assertEquals(profile.name, result.name)
    }

    @Test
    fun `saveProfile throws IllegalArgumentException when PIN is reused`() = runTest {
        val profile = LauncherProfileBuilder.build(id = "profile_test", activationKey = "abc123")
        coEvery { profileDao.isKeyTaken("abc123".mockHash(), profile.id) } returns true

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { roomProfileRepository.saveProfile(profile) }
        }
        coVerify(exactly = 0) { profileDao.saveProfile(any()) }
    }

    @Test
    fun `saveProfile saves when PIN is unique`() = runTest {
        val profile = LauncherProfileBuilder.build(id = "profile_test", activationKey = "abc123")
        coEvery { profileDao.isKeyTaken("abc123".mockHash(), profile.id) } returns false

        roomProfileRepository.saveProfile(profile)
        coVerify(exactly = 1) { profileDao.saveProfile(any()) }
    }

    @Test
    fun `getAppsForProfile returns entities mapped to ProfileAppConfig`() = runTest {
        val crossRef = ProfileAppCrossRef(
            profileId = "profile_test",
            packageName = "com.example.app",
            userHandleNumber = 0L,
            showCountdown = true,
            recommendedUsageMinutes = 15,
        )
        every { profileDao.getAppsForProfile(crossRef.profileId) } returns flowOf(listOf(crossRef))

        val result = roomProfileRepository.getAppsForProfile(crossRef.profileId).first()
        assertEquals(1, result.size)
        assertEquals(crossRef.packageName, result.first().appBasicData.packageName)
        assertEquals(crossRef.userHandleNumber, result.first().appBasicData.userHandleNumber)
    }

    @Test
    fun `getProfileIdsForApp returns a set of profile ids for provided app`() = runTest {
        every {
            profileDao.getProfileIdsForApp("com.example.app", 0L)
        } returns flowOf(listOf("profile_1", "profile_2"))

        val result = roomProfileRepository.getProfileIdsForApp("com.example.app", 0L).first()
        assertEquals(2, result.size)
        assertTrue(result.contains("profile_1"))
        assertTrue(result.contains("profile_2"))
    }

    @Test
    fun `getNotificationAllowedApps combines profile apps and notification whitelist`() = runTest {
        every { profileDao.getAppsForProfile("profile_test") } returns flowOf(
            listOf(ProfileAppCrossRef("profile_test", "com.example.one", 0L))
            )
        every { profileDao.getNotificationWhitelist("profile_test") } returns flowOf(
            listOf(NotificationWhitelistEntity("profile_test", "com.example.two", 0L))
        )

        val result = roomProfileRepository.getNotificationAllowedApps("profile_test").first()
        assertEquals(2, result.size)

        assertTrue(result.contains(AppBasicData("com.example.one", 0L)))
        assertTrue(result.contains(AppBasicData("com.example.two", 0L)))
    }

    @Test
    fun `getNotificationAllowedApps only updates if the app is not in profile app list`() = runTest {
        every { profileDao.getAppsForProfile("profile_test") } returns flowOf(
            listOf(ProfileAppCrossRef("profile_test", "com.example.one", 0L))
        )
        every { profileDao.getNotificationWhitelist("profile_test") } returns flowOf(
            listOf(NotificationWhitelistEntity("profile_test", "com.example.one", 0L))
        )

        val result = roomProfileRepository.getNotificationAllowedApps("profile_test").first()
        assertEquals(1, result.size)
    }

    @Test
    fun `getProfileTriggers maps daysOfWeek from string`() = runTest {
        every { profileDao.getTriggersForProfile("profile_test") } returns flowOf(
            listOf(ProfileTriggerEntityBuilder.build(daysOfWeek = "1,0,2,4"))
        )

        val result = roomProfileRepository.getProfileTriggers("profile_test").first()
        assertEquals(listOf(1, 0, 2, 4), result.first().daysOfWeek)
    }

    @Test
    fun `getProfileTriggers nulls daysOfWeek correctly`() = runTest {
        every { profileDao.getTriggersForProfile("profile_test") } returns flowOf(
            listOf(ProfileTriggerEntityBuilder.build(daysOfWeek = null))
        )

        val result = roomProfileRepository.getProfileTriggers("profile_test").first()
        assertNull(result.first().daysOfWeek)
    }

    @Test
    fun `getProfileTriggers maps profileId and sequenceOrder from entity`() = runTest {
        every { profileDao.getTriggersForProfile("profile_test") } returns flowOf(
            listOf(ProfileTriggerEntityBuilder.build(profileId = "profile_test", sequenceOrder = 3))
        )

        val result = roomProfileRepository.getProfileTriggers("profile_test").first()
        assertEquals("profile_test", result.first().profileId)
        assertEquals(3, result.first().sequenceOrder)
    }
}
