package com.lumina.data.profiles

import kotlinx.coroutines.flow.Flow

interface ProfileDataStore {
    val activeProfileId: Flow<String?>
    suspend fun setActiveProfileId(id: String?)
}
