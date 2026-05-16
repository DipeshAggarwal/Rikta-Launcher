package com.lumina.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lumina.core.database.entity.AppOverrideEntity
import com.lumina.core.model.AppCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface AppOverrideDao {

    @Query("""
        SELECT * FROM app_overrides
        WHERE packageName = :packageName AND userHandleNumber = :userHandleNumber
    """)
    suspend fun get(packageName: String, userHandleNumber: Long): AppOverrideEntity?

    @Query("SELECT * FROM app_overrides")
    fun getAll(): Flow<List<AppOverrideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(app: AppOverrideEntity)

    @Query("""
        DELETE FROM app_overrides
        WHERE packageName = :packageName
        AND userHandleNumber = :userHandleNumber
    """)
    suspend fun delete(packageName: String, userHandleNumber: Long)

    @Query("""
        UPDATE app_overrides
        SET customDisplayName = :displayName
        WHERE packageName = :packageName AND userHandleNumber = :userHandleNumber
    """)
    suspend fun updateDisplayName(packageName: String, userHandleNumber: Long, displayName: String?)

    @Query("""
        UPDATE app_overrides
        SET categoryOverride = :category, customCategoryName = :customCategoryName
        WHERE packageName = :packageName AND userHandleNumber = :userHandleNumber
    """)
    suspend fun updateCategoryName(
        packageName: String, userHandleNumber: Long,
        category: AppCategory?, customCategoryName: String?
    )
}
