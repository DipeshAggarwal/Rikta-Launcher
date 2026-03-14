package com.lumina.domain.shortcut

import com.lumina.domain.shortcut.model.LauncherShortcut
import kotlinx.coroutines.flow.Flow

interface ShortcutRepository {
    fun getAllShortcuts(): Flow<List<LauncherShortcut>>
    fun getDefaultScreenShortcuts(): Flow<List<LauncherShortcut>>
    fun getShortcutsForProfile(profileId: String): Flow<List<LauncherShortcut>>

    suspend fun saveShortcut(shortcut: LauncherShortcut)
    suspend fun updateShortcut(shortcut: LauncherShortcut)
    suspend fun deleteShortcut(shortcutId: String)

    suspend fun addShortcutToProfile(profileId: String, shortcutId: String)
    suspend fun removeShortcutFromProfile(profileId: String, shortcutId: String)

    suspend fun pinToDefault(shortcutId: String)
    suspend fun unpinFromDefault(shortcutId: String)
}
