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
import com.lumina.rikta.startup.InstalledAppsCoordinator
import jakarta.inject.Inject

@HiltAndroidApp
class LauncherApplication: Application() {
    // Injected to trigger the singleton's init block and start background monitoring for the
    // duration of the application process.
    @Inject lateinit var installedAppsCoordinator: InstalledAppsCoordinator
    @Inject lateinit var logger: Logger

    override fun onCreate() {
        super.onCreate()

        // The coordinator is init via Hilt injection, starting the app monitor.
        installedAppsCoordinator

        Migration(this).migrateToUnifiedPrefs()
        // Initialize flavor-specific proxies
        analyticsProxy = AnalyticsProxyImpl()
        messagingInitializer = MessagingInitializerImpl()
        weatherProxy = WeatherImpl()
    }
}
