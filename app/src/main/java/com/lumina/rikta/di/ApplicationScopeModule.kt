package com.lumina.rikta.di

import com.lumina.core.common.IoDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Qualifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob


/**
 * Qualifier for a CoroutineScope that lives as long as the Application process.
 * Used for background tasks like database cleanup or analytics that should not be cancelled when a
 * ViewModel is cleared.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object ApplicationScopeModule {

    /**
     * Provides a CoroutineScope tied to the Application lifecycle.
     * Uses SupervisorJob so that a failure in one child task doesn't kill the whole scope.
     * Uses IoDispatcher by default since most global tasks involve Disk/Network.
     */
    @ApplicationScope
    @Provides
    fun providesApplicationScope(
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)
}
