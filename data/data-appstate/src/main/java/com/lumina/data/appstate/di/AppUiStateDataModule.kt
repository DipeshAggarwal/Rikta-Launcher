package com.lumina.data.appstate.di

import com.lumina.data.appstate.DataStoreAppUiStateRepository
import com.lumina.domain.appstate.AppUiStateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppUiStateDataModule {
    @Binds
    @Singleton
    abstract fun bindsAppUiStateRepository(
        appUiStateRepository: DataStoreAppUiStateRepository
    ): AppUiStateRepository
}
