package com.lumina.data.apps.di

import com.lumina.data.apps.RoomAppOverrideRepository
import com.lumina.data.apps.installed.PackageManagerInstalledAppsRepository
import com.lumina.domain.apps.AppOverrideRepository
import com.lumina.domain.apps.InstalledAppsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InstalledAppsDataModule {

    /**
     * Binds the PackageManager-based implementation to the InstalledAppsRepository interface.
     * Marked as @Singleton to ensure the app-label cache and monitor are shared globally.
     */
    @Binds
    @Singleton
    abstract fun bindInstalledAppsRepository(
        installedAppsRepository: PackageManagerInstalledAppsRepository
    ): InstalledAppsRepository

    @Binds
    @Singleton
    abstract fun bindAppOverrideRepository(
        appOverrideRepository: RoomAppOverrideRepository
    ): AppOverrideRepository
}
