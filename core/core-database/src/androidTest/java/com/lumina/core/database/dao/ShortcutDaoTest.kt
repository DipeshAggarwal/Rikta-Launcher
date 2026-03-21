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
        assertTrue(shortcutDao.getAllShortcuts().first().isEmpty())
    }

    @Test
    fun getAllShortcuts_returns_all_saved_shortcuts() = runTest {
        val shortcutOne = ShortcutEntityBuilder.build("shortcut_1")
        val shortcutTwo = ShortcutEntityBuilder.build("shortcut_2")

        shortcutDao.saveShortcut(shortcutOne)
        shortcutDao.saveShortcut(shortcutTwo)
        assertEquals(2, shortcutDao.getAllShortcuts().first().size)
    }

    @Test
    fun getDefaultScreenShortcuts_returns_only_pinned_shortcuts() = runTest {
        val shortcutOne = ShortcutEntityBuilder.build("shortcut_1", pinnedToDefault = true)
        val shortcutTwo = ShortcutEntityBuilder.build("shortcut_2", pinnedToDefault = false)

        shortcutDao.saveShortcut(shortcutOne)
        shortcutDao.saveShortcut(shortcutTwo)

        val result = shortcutDao.getDefaultScreenShortcuts().first()
        assertEquals(1, result.size)
        assertEquals(shortcutOne.id, result.first().id)
    }

    @Test
    fun getDefaultScreenShortcuts_returns_empty_when_none_pinned() = runTest {
        val shortcut = ShortcutEntityBuilder.build("shortcut_1", pinnedToDefault = false)
        shortcutDao.saveShortcut(shortcut)

        assertTrue(shortcutDao.getDefaultScreenShortcuts().first().isEmpty())
    }

    @Test
    fun getShortcutsForProfile_returns_only_mapped_shortcuts() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")

        val shortcutOne = ShortcutEntityBuilder.build("shortcut_1")
        val shortcutTwo = ShortcutEntityBuilder.build("shortcut_2")

        profileDao.saveProfile(profile)
        shortcutDao.saveShortcut(shortcutOne)
        shortcutDao.saveShortcut(shortcutTwo)
        shortcutDao.addShortcutToProfile(ProfileShortcutCrossRef(profile.id, shortcutTwo.id))

        val result = shortcutDao.getShortcutsForProfile(profile.id).first()
        assertEquals(1, result.size)
        assertEquals(shortcutTwo.id, result.first().id)
    }

    @Test
    fun getShortcutsForProfile_returns_empty_when_no_mappings() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")
        val shortcut = ShortcutEntityBuilder.build("shortcut_1")

        assertTrue(shortcutDao.getShortcutsForProfile(profile.id).first().isEmpty())
    }

    @Test
    fun getShortcutsForProfile_does_not_bleed_across_profiles() = runTest {
        val profileOne = ProfileEntityBuilder.build(id = "profile_test_1")
        val profileTwo = ProfileEntityBuilder.build(id = "profile_test_2")

        val shortcutOne = ShortcutEntityBuilder.build("shortcut_1")
        val shortcutTwo = ShortcutEntityBuilder.build("shortcut_2")

        profileDao.saveProfile(profileOne)
        profileDao.saveProfile(profileTwo)
        shortcutDao.saveShortcut(shortcutOne)
        shortcutDao.saveShortcut(shortcutTwo)

        shortcutDao.addShortcutToProfile(ProfileShortcutCrossRef(profileOne.id, shortcutOne.id))
        shortcutDao.addShortcutToProfile(ProfileShortcutCrossRef(profileTwo.id, shortcutTwo.id))

        val resultOne = shortcutDao.getShortcutsForProfile(profileOne.id).first()
        val resultTwo = shortcutDao.getShortcutsForProfile(profileTwo.id).first()

        assertEquals(1, resultOne.size)
        assertEquals(shortcutOne.id, resultOne.first().id)
        assertEquals(1, resultTwo.size)
        assertEquals(shortcutTwo.id, resultTwo.first().id)
    }

    @Test
    fun saveShortcut_replaces_existing_shortcut_with_same_id() = runTest {
        val shortcutOld = ShortcutEntityBuilder.build("shortcut_1", label = "Old Name")
        val shortcutNew = ShortcutEntityBuilder.build("shortcut_1", label = "New Name")

        shortcutDao.saveShortcut(shortcutOld)
        shortcutDao.saveShortcut(shortcutNew)

        val result = shortcutDao.getAllShortcuts().first()
        assertEquals(1, result.size)
        assertEquals(shortcutNew.label, result.first().label)
    }

    @Test
    fun updateShortcut_modifies_label_of_existing_shortcut() = runTest {
        val shortcutOld = ShortcutEntityBuilder.build("shortcut_1", label = "Old Name")
        val shortcutNew = ShortcutEntityBuilder.build("shortcut_1", label = "New Name")

        shortcutDao.saveShortcut(shortcutOld)
        shortcutDao.updateShortcut(shortcutNew)

        assertEquals(shortcutNew.label, shortcutDao.getAllShortcuts().first().first().label)
    }

    @Test
    fun deleteShortcut_removes_shortcut_from_getAllShortcuts() = runTest {
        val shortcut = ShortcutEntityBuilder.build("shortcut_1")

        shortcutDao.saveShortcut(shortcut)
        shortcutDao.deleteShortcut(shortcut.id)

        assertTrue(shortcutDao.getAllShortcuts().first().isEmpty())
    }

    @Test
    fun deleteShortcut_cascades_to_profile_shortcut_mapping() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")
        val shortcut = ShortcutEntityBuilder.build("shortcut_1")

        profileDao.saveProfile(profile)
        shortcutDao.saveShortcut(shortcut)
        shortcutDao.addShortcutToProfile(ProfileShortcutCrossRef(profile.id, shortcut.id))

        shortcutDao.deleteShortcut(shortcut.id)
        assertTrue(shortcutDao.getShortcutsForProfile(profile.id).first().isEmpty())
    }

    @Test
    fun setPinnedToDefault_true_makes_shortcut_appear_in_defaults() = runTest {
        val shortcut = ShortcutEntityBuilder.build("shortcut_1")

        shortcutDao.saveShortcut(shortcut)
        shortcutDao.setPinnedToDefault(shortcut.id, true)

        val result = shortcutDao.getDefaultScreenShortcuts().first()
        assertEquals(1, result.size)
        assertEquals(shortcut.id, result.first().id)
    }

    @Test
    fun setPinnedToDefault_false_removes_shortcut_from_defaults() = runTest {
        val shortcut = ShortcutEntityBuilder.build("shortcut_1", pinnedToDefault = true)

        shortcutDao.saveShortcut(shortcut)
        shortcutDao.setPinnedToDefault(shortcut.id, false)

        assertTrue(shortcutDao.getDefaultScreenShortcuts().first().isEmpty())
    }

    @Test
    fun removeShortcutFromProfile_removes_mapping_but_preserves_shortcut() = runTest {
        val profile = ProfileEntityBuilder.build(id = "profile_test_1")
        val shortcut = ShortcutEntityBuilder.build("shortcut_1")

        profileDao.saveProfile(profile)
        shortcutDao.saveShortcut(shortcut)
        shortcutDao.addShortcutToProfile(ProfileShortcutCrossRef(profile.id, shortcut.id))
        shortcutDao.removeShortcutFromProfile(profile.id, shortcut.id)

        assertTrue(shortcutDao.getShortcutsForProfile(profile.id).first().isEmpty())
        assertEquals(1, shortcutDao.getAllShortcuts().first().size)
    }

    @Test
    fun removeShortcutFromProfile_does_not_affect_other_profiles() = runTest {
        val profileOne = ProfileEntityBuilder.build(id = "profile_test_1")
        val profileTwo = ProfileEntityBuilder.build(id = "profile_test_2")
        val shortcut = ShortcutEntityBuilder.build("shortcut_1")

        profileDao.saveProfile(profileOne)
        profileDao.saveProfile(profileTwo)
        shortcutDao.saveShortcut(shortcut)

        shortcutDao.addShortcutToProfile(ProfileShortcutCrossRef(profileOne.id, shortcut.id))
        shortcutDao.addShortcutToProfile(ProfileShortcutCrossRef(profileTwo.id, shortcut.id))
        shortcutDao.removeShortcutFromProfile(profileOne.id, shortcut.id)

        assertEquals(1, shortcutDao.getShortcutsForProfile(profileTwo.id).first().size)
    }
}