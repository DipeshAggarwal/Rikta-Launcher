package com.lumina.data.apps.di

import com.lumina.data.apps.datastore.DataStoreHiddenAppsRepository
import com.lumina.domain.apps.HiddenAppsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HiddenAppsDataModule {

    /**
     * Binds the DataStore implementation for hidden apps.
     * A Singleton scope is required so that the 'CleanUp' coordinator and UI always interact with
     * the same data source.
     */
    @Binds
    @Singleton
    abstract fun bindsHiddenRepository(
        hiddenAppsRepository: DataStoreHiddenAppsRepository
    ): HiddenAppsRepository
}
