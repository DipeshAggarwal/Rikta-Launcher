package com.lumina.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

private const val DATA_STORE_NAME = "settings"

// Extension property to ensure a single DataStore instance per process
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

@Module
@InstallIn(SingletonComponent::class)
class DataStoreModule {

    /**
     * Provides the global Preferences DataStore.
     * Marked as @Singleton because creating multiple instances for the same file results in
     * IllegalStateExceptions.
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}
