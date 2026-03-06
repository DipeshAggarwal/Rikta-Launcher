package com.lumina.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lumina.core.database.entity.ProfileShortcutCrossRef
import com.lumina.core.database.entity.ShortcutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcuts")
    fun getAllShortcuts(): Flow<List<ShortcutEntity>>

    @Query("SELECT s.* FROM shortcuts s " +
            "INNER JOIN profile_shortcut_mapping m " +
            "ON s.id = m.shortcutId " +
            "WHERE m.profileId = :profileId"
    )
    fun getShortcutsForProfile(profileId: String): Flow<List<ShortcutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveShortcut(shortcut: ShortcutEntity)

    @Update
    suspend fun updateShortcut(shortcut: ShortcutEntity)

    @Query("DELETE FROM shortcuts WHERE id = :shortcutId")
    suspend fun deleteShortcut(shortcutId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addShortcutToProfile(mapping: ProfileShortcutCrossRef)

    @Query("DELETE FROM profile_shortcut_mapping " +
            "WHERE profileId = :profileId " +
            "AND shortcutId = :shortcutId"
    )
    suspend fun removeShortcutFromProfile(profileId: String, shortcutId: String)
}
