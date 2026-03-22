package com.lumina.data.shortcut

import com.lumina.core.database.dao.ShortcutDao
import com.lumina.core.database.entity.ProfileShortcutCrossRef
import com.lumina.core.database.entity.ShortcutEntity
import com.lumina.core.testing.builder.ShortcutEntityBuilder
import com.lumina.data.shortcut.builder.LauncherShortcutBuilder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RoomShortcutRepositoryTest {
    private lateinit var roomShortcutRepository: RoomShortcutRepository
    private lateinit var shortcutDao: ShortcutDao

    @Before
    fun setup() {
        shortcutDao = mockk(relaxed = true)
        roomShortcutRepository = RoomShortcutRepository(shortcutDao)
    }

    @Test
    fun `getAllShortcuts maps entities to domain`() = runTest {
        every { shortcutDao.getAllShortcuts() } returns flowOf(
            listOf(ShortcutEntityBuilder.build("shortcut_1", "Rikta"))
        )

        val result = roomShortcutRepository.getAllShortcuts().first()
        assertEquals(1, result.size)
        assertEquals("shortcut_1", result.first().id)
        assertEquals("Rikta", result.first().label)
    }

    @Test
    fun `getAllShortcuts returns empty list when DAO returns empty`() = runTest {
        every { shortcutDao.getAllShortcuts() } returns flowOf(emptyList())
        assertTrue(roomShortcutRepository.getAllShortcuts().first().isEmpty())
    }

    @Test
    fun `getDefaultScreenShortcuts maps pinned shortcuts correctly`() = runTest {
        every { shortcutDao.getDefaultScreenShortcuts() } returns flowOf(
            listOf(ShortcutEntityBuilder.build("shortcut_1", pinnedToDefault = true))
        )

        val result = roomShortcutRepository.getDefaultScreenShortcuts().first()
        assertEquals(1, result.size)
        assertTrue(result.first().pinnedToDefault)
    }

    @Test
    fun `getShortcutsForProfile returns all shortcut for profileId`() = runTest {
        every { shortcutDao.getShortcutsForProfile("profile_test") } returns flowOf(
            listOf(ShortcutEntityBuilder.build("shortcut_1"))
        )

        val result = roomShortcutRepository.getShortcutsForProfile("profile_test").first()
        assertEquals(1, result.size)
        assertEquals("shortcut_1", result.first().id)
    }

    @Test
    fun `saveShortcut maps domain model to entity`() = runTest {
        val entitySlot = slot<ShortcutEntity>()
        coEvery { shortcutDao.saveShortcut(capture(entitySlot)) } returns Unit

        roomShortcutRepository.saveShortcut(LauncherShortcutBuilder.build("shortcut_1", "Rikta"))
        assertEquals("shortcut_1", entitySlot.captured.id)
        assertEquals("Rikta", entitySlot.captured.label)
    }

    @Test
    fun `updateShortcut maps domain model to entity`() = runTest {
        val entitySlot = slot<ShortcutEntity>()

        coEvery { shortcutDao.saveShortcut(capture(entitySlot)) } returns Unit
        coEvery { shortcutDao.updateShortcut(capture(entitySlot)) } returns Unit

        roomShortcutRepository.saveShortcut(LauncherShortcutBuilder.build("shortcut_1", "Rikta"))
        roomShortcutRepository.updateShortcut(LauncherShortcutBuilder.build("shortcut_1", "Rikta New"))
        assertEquals("Rikta New", entitySlot.captured.label)
    }

    @Test
    fun `deleteShortcut deletes shortcut`() = runTest {
        roomShortcutRepository.deleteShortcut("shortcut_1")
        coVerify { shortcutDao.deleteShortcut("shortcut_1") }
    }

    @Test
    fun `addShortcutToProfile creates cross ref between profile and shortcut`() = runTest {
        val crossRefSlot = slot<ProfileShortcutCrossRef>()
        coEvery { shortcutDao.addShortcutToProfile(capture(crossRefSlot)) } returns Unit

        roomShortcutRepository.addShortcutToProfile("profile_test", "shortcut_1")
        assertEquals("profile_test", crossRefSlot.captured.profileId)
        assertEquals("shortcut_1", crossRefSlot.captured.shortcutId)
    }

    @Test
    fun `removeShortcutFromProfile removes cross ref between profile and shortcut`() = runTest {
        roomShortcutRepository.removeShortcutFromProfile("profile_test", "shortcut_1")
        coVerify { shortcutDao.removeShortcutFromProfile("profile_test", "shortcut_1") }
    }
}
