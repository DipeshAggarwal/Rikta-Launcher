package com.lumina.rikta

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import com.lumina.rikta.utils.AnalyticsProxyImpl
import com.lumina.rikta.utils.MessagingInitializerImpl
import com.lumina.rikta.utils.WeatherImpl
import com.lumina.rikta.utils.analyticsProxy
import com.lumina.rikta.utils.managers.Migration
import com.lumina.rikta.utils.messagingInitializer
import com.lumina.rikta.utils.weatherProxy
import com.lumina.core.logging.Logger
import jakarta.inject.Inject

@HiltAndroidApp
class LauncherApplication: Application() {
    @Inject lateinit var logger: Logger

    override fun onCreate() {
        super.onCreate()
        logger.d("INIT", "Launcher started and DI is working.")

        Migration(this).migrateToUnifiedPrefs()
        // Initialize flavor-specific proxies
        analyticsProxy = AnalyticsProxyImpl()
        messagingInitializer = MessagingInitializerImpl()
        weatherProxy = WeatherImpl()
    }
}
