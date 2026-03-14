package com.lumina.data.shortcut

import com.lumina.core.database.dao.ShortcutDao
import com.lumina.core.database.entity.ProfileShortcutCrossRef
import com.lumina.core.database.entity.ShortcutEntity
import com.lumina.domain.shortcut.ShortcutRepository
import com.lumina.domain.shortcut.model.LauncherShortcut
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomShortcutRepository @Inject constructor(
    private val shortcutDao: ShortcutDao
) : ShortcutRepository{
    private val TAG = this::class.java.simpleName

    private fun ShortcutEntity.toDomain() = LauncherShortcut(
        id = id,
        label = label,
        pinnedToDefault = pinnedToDefault,
        type = type,
        shortcutPackage = shortcutPackage,
        shortcutId = shortcutId,
        url = url,
        targetPackage = targetPackage
    )

    private fun LauncherShortcut.toEntity() = ShortcutEntity(
        id = id,
        label = label,
        pinnedToDefault = pinnedToDefault,
        type = type,
        shortcutPackage = shortcutPackage,
        shortcutId = shortcutId,
        url = url,
        targetPackage = targetPackage
    )

    override fun getAllShortcuts(): Flow<List<LauncherShortcut>> {
        return shortcutDao.getAllShortcuts().map { it.map { it.toDomain() } }
    }

    override fun getDefaultScreenShortcuts(): Flow<List<LauncherShortcut>> {
        return shortcutDao.getDefaultScreenShortcuts().map { it.map { it.toDomain() } }
    }

    override fun getShortcutsForProfile(profileId: String): Flow<List<LauncherShortcut>> {
        return shortcutDao.getShortcutsForProfile(profileId).map { it.map { it.toDomain() } }
    }

    override suspend fun saveShortcut(shortcut: LauncherShortcut) {
        shortcutDao.saveShortcut(shortcut.toEntity())
    }

    override suspend fun updateShortcut(shortcut: LauncherShortcut) {
        shortcutDao.updateShortcut(shortcut.toEntity())
    }

    override suspend fun deleteShortcut(shortcutId: String) {
        shortcutDao.deleteShortcut(shortcutId)
    }

    override suspend fun addShortcutToProfile(profileId: String, shortcutId: String) {
        shortcutDao.addShortcutToProfile(
            ProfileShortcutCrossRef(profileId, shortcutId)
        )
    }

    override suspend fun removeShortcutFromProfile(
        profileId: String,
        shortcutId: String
    ) {
        shortcutDao.removeShortcutFromProfile(profileId, shortcutId)
    }

    override suspend fun pinToDefault(shortcutId: String) {
        shortcutDao.setPinnedToDefault(shortcutId, true)
    }

    override suspend fun unpinFromDefault(shortcutId: String) {
        shortcutDao.setPinnedToDefault(shortcutId, false)
    }
}
