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

private const val DATA_STORE_SETTINGS_NAME = "settings"
private const val DATA_STORE_APP_UI_STATE_NAME = "app_ui_state"

// Extension property to ensure a single DataStore instance per process
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_SETTINGS_NAME)
private val Context.appUiStateDataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_APP_UI_STATE_NAME)

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /**
     * Provides the global Preferences DataStore.
     * Marked as @Singleton because creating multiple instances for the same file results in
     * IllegalStateExceptions.
     */
    @Provides
    @Singleton
    @SettingsDataStore
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.settingsDataStore
    }

    @Provides
    @Singleton
    @AppUiStateDataStore
    fun provideAppUiStateDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.appUiStateDataStore
    }
}
