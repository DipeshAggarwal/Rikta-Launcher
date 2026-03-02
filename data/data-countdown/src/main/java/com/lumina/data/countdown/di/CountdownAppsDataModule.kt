package com.lumina.data.countdown.di

import com.lumina.data.countdown.DataStoreCountdownRepository
import com.lumina.domain.countdown.CountdownAppsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CountdownAppsDataModule {

    @Binds
    @Singleton
    abstract fun bindsCountdownRepository(
        dataStoreCountdownRepository: DataStoreCountdownRepository
    ): CountdownAppsRepository
}
