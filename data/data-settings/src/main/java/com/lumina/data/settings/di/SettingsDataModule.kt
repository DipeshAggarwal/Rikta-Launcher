package com.lumina.data.settings.di

import com.lumina.data.settings.DataStoreSettingsRepository
import com.lumina.domain.settings.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsDataModule {
    @Binds
    @Singleton
    abstract fun bindsSettingsRepository(
        settingsRepository: DataStoreSettingsRepository
    ): SettingsRepository
}
