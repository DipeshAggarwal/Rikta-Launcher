package com.lumina.data.shortcut

import com.lumina.core.database.dao.ShortcutDao
import com.lumina.core.database.entity.ProfileShortcutCrossRef
import com.lumina.core.database.entity.ShortcutEntity
import com.lumina.domain.shortcut.ShortcutRepository
import com.lumina.core.model.LauncherShortcut
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
        type = type,
        userHandleNumber = userHandleNumber,
        shortcutPackage = shortcutPackage,
        shortcutId = shortcutId,
        url = url,
        targetPackage = targetPackage
    )

    private fun LauncherShortcut.toEntity() = ShortcutEntity(
        id = id,
        label = label,
        type = type,
        userHandleNumber = userHandleNumber,
        shortcutPackage = shortcutPackage,
        shortcutId = shortcutId,
        url = url,
        targetPackage = targetPackage
    )

    override fun get(profileId: String): Flow<List<LauncherShortcut>> {
        return shortcutDao.get(profileId).map { it }
    }

    override fun getAll(): Flow<List<LauncherShortcut>> {
        return shortcutDao.getAll().map { it }
    }

    override suspend fun save(shortcut: LauncherShortcut) {
        shortcutDao.save(shortcut.toEntity())
    }

    override suspend fun update(shortcut: LauncherShortcut) {
        shortcutDao.update(shortcut.toEntity())
    }

    override suspend fun delete(shortcutId: String) {
        shortcutDao.delete(shortcutId)
    }

    override suspend fun addToProfile(profileId: String, shortcutId: String) {
        shortcutDao.addToProfile(
            ProfileShortcutCrossRef(profileId, shortcutId)
        )
    }

    override suspend fun removeFromProfile(
        profileId: String,
        shortcutId: String
    ) {
        shortcutDao.removeFromProfile(profileId, shortcutId)
    }
}
