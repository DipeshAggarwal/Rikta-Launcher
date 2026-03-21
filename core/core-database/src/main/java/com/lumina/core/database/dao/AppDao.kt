package com.lumina.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lumina.core.database.entity.AppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addApp(app: AppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addApps(apps: List<AppEntity>)

    @Query("DELETE FROM apps WHERE packageName = :packageName AND userHandleNumber = :userHandleNumber")
    suspend fun deleteApp(packageName: String, userHandleNumber: Long)

    @Query("SELECT * FROM apps")
    fun getAllApps(): Flow<List<AppEntity>>
}
