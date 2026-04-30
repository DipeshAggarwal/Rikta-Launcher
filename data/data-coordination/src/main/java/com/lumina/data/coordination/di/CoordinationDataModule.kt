package com.lumina.data.coordination.di

import com.lumina.data.coordination.PlatformAppLaunchCoordinator
import com.lumina.data.coordination.PlatformAppShortcutRepository
import com.lumina.data.coordination.PlatformDeviceUserProvider
import com.lumina.data.coordination.PlatformIntentLauncher
import com.lumina.data.coordination.PlatformStatusBarController
import com.lumina.domain.apps.AppShortcutRepository
import com.lumina.domain.coordination.AppLaunchCoordinator
import com.lumina.domain.coordination.DeviceUserProvider
import com.lumina.domain.coordination.IntentLauncher
import com.lumina.domain.coordination.StatusBarController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoordinationDataModule {

    @Binds
    @Singleton
    abstract fun bindsIntentLauncher(platformIntentLauncher: PlatformIntentLauncher): IntentLauncher

    @Binds
    @Singleton
    abstract fun bindsStatusBarController(
        platformStatusBarController: PlatformStatusBarController
    ): StatusBarController

    @Binds
    @Singleton
    abstract fun bindsAppShortcutManager(
        platformAppShortcutRepository: PlatformAppShortcutRepository
    ): AppShortcutRepository

    @Binds
    @Singleton
    abstract fun bindsAppLaunchCoordinator(
        platformAppLaunchCoordinator: PlatformAppLaunchCoordinator
    ): AppLaunchCoordinator

    @Binds
    @Singleton
    abstract fun bindsDeviceUserProvider(
        platformDeviceUserProvider: PlatformDeviceUserProvider
    ): DeviceUserProvider
}
