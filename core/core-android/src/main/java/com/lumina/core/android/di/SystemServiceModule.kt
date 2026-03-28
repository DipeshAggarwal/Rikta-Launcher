package com.lumina.core.android.di

import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SystemServiceModule {

    @Provides
    @Singleton
    fun providesUserManager(@ApplicationContext context: Context): UserManager {
        return context.getSystemService(Context.USER_SERVICE) as UserManager
    }

    @Provides
    @Singleton
    fun providesLauncherApps(@ApplicationContext context: Context): LauncherApps {
        return context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    }

    @Provides
    @Singleton
    fun providesWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}
