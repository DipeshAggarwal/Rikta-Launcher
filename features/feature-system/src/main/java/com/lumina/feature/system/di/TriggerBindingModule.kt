package com.lumina.feature.system.di

import com.lumina.core.common.BootHandler
import com.lumina.feature.system.triggers.TriggerBootHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class TriggerBindingModule {

    @Binds
    @IntoSet
    abstract fun bindsTriggerBootHandler(
        bootHandler: TriggerBootHandler
    ): BootHandler
}
