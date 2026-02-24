package com.lumina.data.apps.di

import com.lumina.data.apps.datastore.DataStoreFavouriteAppsRepository
import com.lumina.domain.apps.FavouriteAppsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FavouriteAppsDataModule {

    /**
     * Binds the DataStore implementation for favorite apps.
     * Uses @Singleton to ensure all ViewModels observe the same favorite list state.
     */
    @Binds
    @Singleton
    abstract fun bindsFavouriteRepository(
        favouriteAppsRepository: DataStoreFavouriteAppsRepository
    ): FavouriteAppsRepository
}
