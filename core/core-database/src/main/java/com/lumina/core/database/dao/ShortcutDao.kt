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

    @Query("SELECT s.* FROM shortcuts s " +
            "INNER JOIN profile_shortcut_mapping m " +
            "ON s.id = m.shortcutId " +
            "WHERE m.profileId = :profileId"
    )
    fun get(profileId: String): Flow<List<ShortcutEntity>>
    @Query("SELECT * FROM shortcuts")
    fun getAll(): Flow<List<ShortcutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(shortcut: ShortcutEntity)

    @Update
    suspend fun update(shortcut: ShortcutEntity)

    @Query("DELETE FROM shortcuts WHERE id = :shortcutId")
    suspend fun delete(shortcutId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToProfile(mapping: ProfileShortcutCrossRef)

    @Query("DELETE FROM profile_shortcut_mapping " +
            "WHERE profileId = :profileId AND shortcutId = :shortcutId"
    )
    suspend fun removeFromProfile(profileId: String, shortcutId: String)
}
