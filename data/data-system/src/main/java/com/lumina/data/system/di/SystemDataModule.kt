package com.lumina.data.system.di

import com.lumina.data.system.PlatformAppLaunchCoordinator
import com.lumina.data.system.PlatformAppShortcutRepository
import com.lumina.data.system.PlatformIntentLauncher
import com.lumina.data.system.PlatformStatusBarController
import com.lumina.domain.apps.AppShortcutRepository
import com.lumina.domain.system.AppLaunchCoordinator
import com.lumina.domain.system.IntentLauncher
import com.lumina.domain.system.StatusBarController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SystemDataModule {

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
}
