package com.lumina.domain.shortcut

import com.lumina.core.model.LauncherShortcut
import kotlinx.coroutines.flow.Flow

interface ShortcutRepository {
    fun get(profileId: String): Flow<List<LauncherShortcut>>
    fun getAll(): Flow<List<LauncherShortcut>>

    suspend fun save(shortcut: LauncherShortcut)
    suspend fun update(shortcut: LauncherShortcut)
    suspend fun delete(shortcutId: String)

    suspend fun addToProfile(profileId: String, shortcutId: String)
    suspend fun removeFromProfile(profileId: String, shortcutId: String)
}
