package com.data.system.di

import com.data.system.PlatformIntentLauncher
import com.data.system.PlatformStatusBarController
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
}
