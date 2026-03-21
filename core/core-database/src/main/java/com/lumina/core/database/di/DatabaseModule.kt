package com.lumina.core.database.di

import android.content.Context
import androidx.room.Room
import com.lumina.core.database.LuminaDatabase
import com.lumina.core.database.dao.AppDao
import com.lumina.core.database.dao.AppUsageDao
import com.lumina.core.database.dao.ProfileDao
import com.lumina.core.database.dao.ShortcutDao
import com.lumina.core.database.migration.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesAppDatabase(@ApplicationContext context: Context): LuminaDatabase {
        return Room.databaseBuilder(
            context,
            LuminaDatabase::class.java,
            "lumina_rikta_database"
        )
            .addMigrations(DatabaseMigrations.MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun providesProfileDao(database: LuminaDatabase): ProfileDao {
        return database.profileDao()
    }

    @Provides
    @Singleton
    fun providesAppDao(database: LuminaDatabase): AppDao {
        return database.appDao()
    }

    @Provides
    @Singleton
    fun providesShortcutDao(database: LuminaDatabase): ShortcutDao {
        return database.shortcutDao()
    }

    @Provides
    @Singleton
    fun providesAppUsageDao(database: LuminaDatabase): AppUsageDao {
        return database.appUsageDao()
    }
}
