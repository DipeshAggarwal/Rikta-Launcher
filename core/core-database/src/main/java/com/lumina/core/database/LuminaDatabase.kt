package com.lumina.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lumina.core.database.dao.ProfileDao
import com.lumina.core.database.entity.NotificationWhitelistEntity
import com.lumina.core.database.entity.ProfileAppCrossRef
import com.lumina.core.database.entity.ProfileEntity
import com.lumina.core.database.entity.ProfileTriggerEntity

@Database(
    entities = [
        ProfileEntity::class,
        ProfileAppCrossRef::class,
        ProfileTriggerEntity::class,
        NotificationWhitelistEntity::class
    ],
    version = DatabaseConstants.DATABASE_VERSION,
    exportSchema = true
)

abstract class LuminaDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
}
