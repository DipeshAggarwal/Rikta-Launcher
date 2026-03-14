package com.lumina.core.android.di

import android.content.Context
import android.os.UserManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserManagerModule {

    @Provides
    @Singleton
    fun providesUserManager(@ApplicationContext context: Context): UserManager {
        return context.getSystemService(UserManager::class.java)
    }
}
