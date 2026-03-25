package com.lumina.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lumina.core.database.LuminaDatabase
import com.lumina.core.database.entity.ProfileShortcutCrossRef
import com.lumina.core.testing.builder.ProfileEntityBuilder
import com.lumina.core.testing.builder.ShortcutEntityBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.collections.first

@RunWith(AndroidJUnit4::class)
class ShortcutDaoTest {
    private lateinit var database: LuminaDatabase
    private lateinit var profileDao: ProfileDao
    private lateinit var shortcutDao: ShortcutDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LuminaDatabase::class.java
        ).build()
        profileDao = database.profileDao()
        shortcutDao = database.shortcutDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getAllShortcuts_returns_empty_on_fresh_database() = runTest {
        assertTrue(shortcutDao.getAll().first().isEmpty())
    }

    @Test
    fun getAllShortcuts_returns_all_saved_shortcuts() = runTest {
        val shortcutOne = ShortcutEntityBuilder.build("shortcut_1")
        val shortcutTwo = ShortcutEntityBuilder.build("shortcut_2")

        shortcutDao.save(shortcutOne)
        shortcutDao.save(shortcutTwo)
        assertEquals(2, shortcutDao.getAll().first().size)
    }

    @Test
    fun getShortcutsForProfile_returns_only_mapped_shortcuts() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")

        val shortcutOne = ShortcutEntityBuilder.build("shortcut_1")
        val shortcutTwo = ShortcutEntityBuilder.build("shortcut_2")

        profileDao.saveProfile(profile)
        shortcutDao.save(shortcutOne)
        shortcutDao.save(shortcutTwo)
        shortcutDao.addToProfile(ProfileShortcutCrossRef(profile.id, shortcutTwo.id))

        val result = shortcutDao.get(profile.id).first()
        assertEquals(1, result.size)
        assertEquals(shortcutTwo.id, result.first().id)
    }

    @Test
    fun getShortcutsForProfile_returns_empty_when_no_mappings() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")
        val shortcut = ShortcutEntityBuilder.build("shortcut_1")

        assertTrue(shortcutDao.get(profile.id).first().isEmpty())
    }

    @Test
    fun getShortcutsForProfile_does_not_bleed_across_profiles() = runTest {
        val profileOne = ProfileEntityBuilder.build(id = "profile_test_1")
        val profileTwo = ProfileEntityBuilder.build(id = "profile_test_2")

        val shortcutOne = ShortcutEntityBuilder.build("shortcut_1")
        val shortcutTwo = ShortcutEntityBuilder.build("shortcut_2")

        profileDao.saveProfile(profileOne)
        profileDao.saveProfile(profileTwo)
        shortcutDao.save(shortcutOne)
        shortcutDao.save(shortcutTwo)

        shortcutDao.addToProfile(ProfileShortcutCrossRef(profileOne.id, shortcutOne.id))
        shortcutDao.addToProfile(ProfileShortcutCrossRef(profileTwo.id, shortcutTwo.id))

        val resultOne = shortcutDao.get(profileOne.id).first()
        val resultTwo = shortcutDao.get(profileTwo.id).first()

        assertEquals(1, resultOne.size)
        assertEquals(shortcutOne.id, resultOne.first().id)
        assertEquals(1, resultTwo.size)
        assertEquals(shortcutTwo.id, resultTwo.first().id)
    }

    @Test
    fun saveShortcut_replaces_existing_shortcut_with_same_id() = runTest {
        val shortcutOld = ShortcutEntityBuilder.build("shortcut_1", label = "Old Name")
        val shortcutNew = ShortcutEntityBuilder.build("shortcut_1", label = "New Name")

        shortcutDao.save(shortcutOld)
        shortcutDao.save(shortcutNew)

        val result = shortcutDao.getAll().first()
        assertEquals(1, result.size)
        assertEquals(shortcutNew.label, result.first().label)
    }

    @Test
    fun updateShortcut_modifies_label_of_existing_shortcut() = runTest {
        val shortcutOld = ShortcutEntityBuilder.build("shortcut_1", label = "Old Name")
        val shortcutNew = ShortcutEntityBuilder.build("shortcut_1", label = "New Name")

        shortcutDao.save(shortcutOld)
        shortcutDao.update(shortcutNew)

        assertEquals(shortcutNew.label, shortcutDao.getAll().first().first().label)
    }

    @Test
    fun deleteShortcut_removes_shortcut_from_getAllShortcuts() = runTest {
        val shortcut = ShortcutEntityBuilder.build("shortcut_1")

        shortcutDao.save(shortcut)
        shortcutDao.delete(shortcut.id)

        assertTrue(shortcutDao.getAll().first().isEmpty())
    }

    @Test
    fun deleteShortcut_cascades_to_profile_shortcut_mapping() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")
        val shortcut = ShortcutEntityBuilder.build("shortcut_1")

        profileDao.saveProfile(profile)
        shortcutDao.save(shortcut)
        shortcutDao.addToProfile(ProfileShortcutCrossRef(profile.id, shortcut.id))

        shortcutDao.delete(shortcut.id)
        assertTrue(shortcutDao.get(profile.id).first().isEmpty())
    }

    @Test
    fun removeShortcutFromProfile_removes_mapping_but_preserves_shortcut() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")
        val shortcut = ShortcutEntityBuilder.build("shortcut_1")

        profileDao.saveProfile(profile)
        shortcutDao.save(shortcut)
        shortcutDao.addToProfile(ProfileShortcutCrossRef(profile.id, shortcut.id))
        shortcutDao.removeFromProfile(profile.id, shortcut.id)

        assertTrue(shortcutDao.get(profile.id).first().isEmpty())
        assertEquals(1, shortcutDao.getAll().first().size)
    }

    @Test
    fun removeShortcutFromProfile_does_not_affect_other_profiles() = runTest {
        val profileOne = ProfileEntityBuilder.build(id = "profile_test_1")
        val profileTwo = ProfileEntityBuilder.build(id = "profile_test_2")
        val shortcut = ShortcutEntityBuilder.build("shortcut_1")

        profileDao.saveProfile(profileOne)
        profileDao.saveProfile(profileTwo)
        shortcutDao.save(shortcut)

        shortcutDao.addToProfile(ProfileShortcutCrossRef(profileOne.id, shortcut.id))
        shortcutDao.addToProfile(ProfileShortcutCrossRef(profileTwo.id, shortcut.id))
        shortcutDao.removeFromProfile(profileOne.id, shortcut.id)

        assertEquals(1, shortcutDao.get(profileTwo.id).first().size)
    }
}