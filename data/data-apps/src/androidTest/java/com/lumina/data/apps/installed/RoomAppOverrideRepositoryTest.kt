package com.lumina.data.apps.installed

import androidx.room.withTransaction
import com.lumina.core.database.LuminaDatabase
import com.lumina.core.database.dao.AppOverrideDao
import com.lumina.core.model.AppCategory
import com.lumina.core.testing.builder.AppOverrideEntityBuilder
import com.lumina.data.apps.RoomAppOverrideRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class RoomAppOverrideRepositoryTest {
    private lateinit var database: LuminaDatabase
    private lateinit var appOverrideDao: AppOverrideDao
    private lateinit var roomAppOverrideRepository: RoomAppOverrideRepository

    @Before
    fun setup() {
        database = mockk(relaxed = true)
        appOverrideDao = mockk(relaxed = true)

        roomAppOverrideRepository = RoomAppOverrideRepository(database, appOverrideDao)
        mockkStatic("androidx.room.RoomDatabaseKt")

        val transactionSlot = slot<suspend () -> Any>()
        coEvery { database.withTransaction(capture(transactionSlot)) } coAnswers {
            transactionSlot.captured.invoke()
        }
    }

    @After
    fun teardown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun getAll_returns_database_entities_mapped_to_AppOverride() = runTest {
        val entity = AppOverrideEntityBuilder.build(
            packageName = "com.example.app",
            userHandleNumber = 0L,
        )
        every { appOverrideDao.getAll() } returns flowOf(listOf(entity))

        val result = roomAppOverrideRepository.allOverrides.first()
        assertEquals(1, result.size)
        assertEquals(entity.packageName, result.first().packageName)
        assertEquals(entity.userHandleNumber, result.first().userHandleNumber)
    }

    @Test
    fun get_returns_database_entity_mapped_to_AppOverride() = runTest {
        val entity = AppOverrideEntityBuilder.build(
            packageName = "com.example.app",
            userHandleNumber = 0L,
        )
        coEvery { appOverrideDao.get(entity.packageName, entity.userHandleNumber) } returns entity

        val result = roomAppOverrideRepository.get(entity.packageName, entity.userHandleNumber)

        assertEquals(entity.packageName, result!!.packageName)
        assertEquals(entity.userHandleNumber, result.userHandleNumber)
    }

    @Test
    fun setDisplayName_updates_record_when_it_exists() = runTest {
        val entity = AppOverrideEntityBuilder.build(
            packageName = "com.example.app",
            userHandleNumber = 0L,
        )
        val newName = "New Name"

        coEvery { appOverrideDao.get(entity.packageName, entity.userHandleNumber) } returns entity
        roomAppOverrideRepository.setDisplayName(entity.packageName, entity.userHandleNumber, newName)

        coVerify(exactly = 1) { appOverrideDao.updateDisplayName(entity.packageName, entity.userHandleNumber, newName) }
        coVerify(exactly = 0) { appOverrideDao.save(any()) }

    }

    @Test
    fun setDisplayName_creates_record_when_it_does_not_exist() = runTest {
        val entity = AppOverrideEntityBuilder.build(
            packageName = "com.example.app",
            userHandleNumber = 0L,
        )
        val newName = "New Name"

        coEvery { appOverrideDao.get(entity.packageName, entity.userHandleNumber) } returns null
        roomAppOverrideRepository.setDisplayName(entity.packageName, entity.userHandleNumber, newName)

        coVerify(exactly = 0) { appOverrideDao.updateDisplayName(entity.packageName, entity.userHandleNumber, newName) }
        coVerify(exactly = 1) { appOverrideDao.save(match {
            it.packageName == entity.packageName &&
                    it.userHandleNumber == entity.userHandleNumber &&
                    it.customDisplayName == newName
        }) }
    }

    @Test
    fun setCategory_updates_record_when_it_exists() = runTest {
        val entity = AppOverrideEntityBuilder.build(
            packageName = "com.example.app",
            userHandleNumber = 0L,
        )
        val newCategory = AppCategory.FINANCE

        coEvery { appOverrideDao.get(entity.packageName, entity.userHandleNumber) } returns entity
        roomAppOverrideRepository.setCategory(entity.packageName, entity.userHandleNumber, newCategory)

        coVerify(exactly = 1) { appOverrideDao.updateCategoryName(entity.packageName, entity.userHandleNumber, newCategory, null) }
        coVerify(exactly = 0) { appOverrideDao.save(any()) }

    }

    @Test
    fun setCategory_creates_record_when_it_does_not_exist() = runTest {
        val entity = AppOverrideEntityBuilder.build(
            packageName = "com.example.app",
            userHandleNumber = 0L,
        )
        val newCategory = AppCategory.FINANCE

        coEvery { appOverrideDao.get(entity.packageName, entity.userHandleNumber) } returns null
        roomAppOverrideRepository.setCategory(entity.packageName, entity.userHandleNumber, newCategory)

        coVerify(exactly = 0) { appOverrideDao.updateCategoryName(entity.packageName, entity.userHandleNumber, newCategory, null) }
        coVerify(exactly = 1) { appOverrideDao.save(match {
            it.packageName == entity.packageName &&
            it.userHandleNumber == entity.userHandleNumber &&
            it.categoryOverride == newCategory
        }) }
    }
}