package com.lumina.data.usage.di

import com.lumina.core.common.BootHandler
import com.lumina.data.usage.DataStoreUsageSettingsRepository
import com.lumina.data.usage.DefaultAppUsageTracker
import com.lumina.data.usage.RoomUsageRepository
import com.lumina.data.usage.UsageBootHandler
import com.lumina.domain.usage.AppUsageTracker
import com.lumina.domain.usage.UsageRepository
import com.lumina.domain.usage.UsageSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UsageModule {

    @Binds
    @Singleton
    abstract fun bindsUsageRepository(usageRepository: RoomUsageRepository): UsageRepository

    @Binds
    @Singleton
    abstract fun bindsAppUsageEventListener(
        appUsageTracker: DefaultAppUsageTracker
    ): AppUsageTracker

    @Binds
    @Singleton
    abstract fun bindsUsageSettingsRepository(
        usageSettingsRepository: DataStoreUsageSettingsRepository
    ): UsageSettingsRepository

    @Binds
    @IntoSet
    abstract fun bindsUsageBootHandler(usageBootHandler: UsageBootHandler): BootHandler
}
