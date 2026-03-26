package com.lumina.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lumina.core.database.LuminaDatabase
import com.lumina.core.database.entity.AppOverrideEntity
import com.lumina.core.model.AppCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppOverrideDaoTest {
    private lateinit var database: LuminaDatabase
    private lateinit var appOverrideDao: AppOverrideDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LuminaDatabase::class.java
        ).build()
        appOverrideDao = database.appOverrideDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun save_inserts_and_gets_entity() = runTest {
        val override = AppOverrideEntity(
            "com.example.app", 0L,
            AppCategory.CUSTOM, "Null", "Custom App")

        appOverrideDao.save(override)
        val result = appOverrideDao.getAll().first()

        assertEquals(1, result.size)
        assertEquals("Custom App", result.first().customDisplayName)
    }

    @Test
    fun delete_removes_entity() = runTest {
        val override = AppOverrideEntity(
            "com.example.app", 0L,
            AppCategory.CUSTOM, "Null", "Custom App")

        appOverrideDao.save(override)
        appOverrideDao.delete(override.packageName, override.userHandleNumber)

        assertTrue(appOverrideDao.getAll().first().isEmpty())
    }

    @Test
    fun updateDisplayName_modifies_only_name() = runTest {
        val override = AppOverrideEntity(
            "com.example.app", 0L,
            AppCategory.CUSTOM, "Null", "Custom App"
        )

        appOverrideDao.save(override)
        appOverrideDao.updateDisplayName(override.packageName, override.userHandleNumber, "New Name")

        assertEquals("New Name", appOverrideDao.get(override.packageName, override.userHandleNumber)!!.customDisplayName)
    }

    @Test
    fun updateCategoryName_modifies_only_name() = runTest {
        val override = AppOverrideEntity(
            "com.example.app", 0L,
            AppCategory.CUSTOM, "Null", "Custom App"
        )

        appOverrideDao.save(override)
        appOverrideDao.updateCategoryName(
            override.packageName, override.userHandleNumber,
            AppCategory.ENTERTAINMENT, null
        )

        assertEquals(AppCategory.ENTERTAINMENT, appOverrideDao.get(override.packageName, override.userHandleNumber)!!.categoryOverride)
    }
}
