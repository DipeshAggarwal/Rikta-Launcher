package com.lumina.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lumina.core.database.dao.AppDao
import com.lumina.core.database.dao.AppUsageDao
import com.lumina.core.database.dao.ProfileDao
import com.lumina.core.database.dao.ShortcutDao
import com.lumina.core.database.entity.AppEntity
import com.lumina.core.database.entity.AppUsageSessionEntity
import com.lumina.core.database.entity.NotificationWhitelistEntity
import com.lumina.core.database.entity.ProfileAppCrossRef
import com.lumina.core.database.entity.ProfileEntity
import com.lumina.core.database.entity.ProfileShortcutCrossRef
import com.lumina.core.database.entity.ProfileSwitchLogEntity
import com.lumina.core.database.entity.ProfileTriggerEntity
import com.lumina.core.database.entity.ShortcutEntity

@Database(
    entities = [
        ProfileEntity::class,
        ProfileAppCrossRef::class,
        ProfileTriggerEntity::class,
        NotificationWhitelistEntity::class,

        AppEntity::class,

        ShortcutEntity::class,
        ProfileShortcutCrossRef::class,

        AppUsageSessionEntity::class,
        ProfileSwitchLogEntity::class
    ],
    version = DatabaseConstants.DATABASE_VERSION,
    exportSchema = true
)

abstract class LuminaDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun appDao(): AppDao
    abstract fun shortcutDao(): ShortcutDao
    abstract fun appUsageDao(): AppUsageDao
}
