package com.lumina.data.usage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class DataStoreUsageSettingsRepositoryTest {
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var dataStoreUsageSettingsRepository: DataStoreUsageSettingsRepository

    @Before
    fun setup() {
        dataStore = mockk(relaxed = true)
        dataStoreUsageSettingsRepository = DataStoreUsageSettingsRepository(dataStore)
    }

    @Test
    fun `rawRetentionDays defaults to 14 when key absent`() = runTest {
        val prefs = mockk<Preferences>()

        every { prefs[intPreferencesKey("usage_raw_retention_days")] } returns null
        every { dataStore.data } returns flowOf(prefs)

        assertEquals(14, dataStoreUsageSettingsRepository.rawRetentionDays.first())
    }

    @Test
    fun `rawRetentionDays returns stored value when key present`() = runTest {
        val prefs = mockk<Preferences>()

        every { prefs[intPreferencesKey("usage_raw_retention_days")] } returns 30
        every { dataStore.data } returns flowOf(prefs)

        assertEquals(30, dataStoreUsageSettingsRepository.rawRetentionDays.first())
    }

    @Test
    fun `setRawRetentionDays throws IllegalArgumentException for zero`() = runTest {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { dataStoreUsageSettingsRepository.setRawRetentionDays(0) }
        }
    }

    @Test
    fun `setRawRetentionDays throws IllegalArgumentException for negative value`() = runTest {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { dataStoreUsageSettingsRepository.setRawRetentionDays(-1) }
        }
    }
}
