package com.lumina.core.logging.di

import com.lumina.core.logging.AndroidLogger
import com.lumina.core.logging.Logger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoggingModule {

    @Binds
    @Singleton
    abstract fun bindAndroidLogger(androidLogger: AndroidLogger): Logger
}
