package com.lumina.data.profiles

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ProfileDataStoreRepositoryTest {
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var profileDataStoreRepository: ProfileDataStoreRepository

    @Before
    fun setup() {
        dataStore = mockk(relaxed = true)
        profileDataStoreRepository = ProfileDataStoreRepository(dataStore)
    }

    @Test
    fun `activeProfileId returns null when key is not present`() = runTest {
        val prefs = mockk<Preferences>()

        every { prefs[stringPreferencesKey("active_profile_id")] } returns null
        every { dataStore.data } returns flowOf(prefs)

        assertNull(profileDataStoreRepository.activeProfileId.first())
    }

    @Test
    fun `activeProfileId returns stored id when key is present`() = runTest {
        val prefs = mockk<Preferences>()

        every { prefs[stringPreferencesKey("active_profile_id")] } returns "profile_test"
        every { dataStore.data } returns flowOf(prefs)

        assertEquals("profile_test", profileDataStoreRepository.activeProfileId.first())
    }
}
