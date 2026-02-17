package com.lumina.core.android

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ContextModule {

    /**
     * Provides a standard Context binding by unwrapping the Hilt @ApplicationContext.
     * This simplifies injection for classes that don't want to rely on Hilt-specific annotations.
     */
    @Provides
    fun provideApplicationContext(
        @ApplicationContext context: Context
    ): Context = context
}