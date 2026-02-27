package com.lumina.data.system

import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.UserManager
import android.provider.AlarmClock
import androidx.core.net.toUri
import com.lumina.core.logging.Logger
import com.lumina.domain.apps.AppInfo
import com.lumina.domain.apps.AppShortcut
import com.lumina.domain.system.IntentLauncher
import com.lumina.domain.system.LaunchResult
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class PlatformIntentLauncher @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val logger: Logger
) : IntentLauncher {
    private val TAG = this::class.java.simpleName
    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val userManager = context.getSystemService(UserManager::class.java)

    override suspend fun openApp(app: AppInfo): LaunchResult {
        return try {
            val componentName = ComponentName(app.packageName, app.componentClassName)
            val userHandle = userManager.getUserForSerialNumber(app.userHandleNumber)
                ?: return LaunchResult.NoLaunchIntent

            launcherApps.startMainActivity(
                componentName,
                userHandle,
                Rect(),
                ActivityOptions.makeBasic().toBundle()
            )
            LaunchResult.Success
        } catch (e: SecurityException) {
            logger.w("$TAG:App", "Security Exception launching ${app.packageName}.", e)

            val intent = context.packageManager
                .getLaunchIntentForPackage(app.packageName)
                ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

            if (intent == null) {
                logger.e("$TAG:App", "Fallback launch also failed for ${app.packageName}.")
                LaunchResult.NoLaunchIntent
            } else {
                context.startActivity(intent)
                LaunchResult.Success
            }
        } catch (e: Exception) {
            logger.e("$TAG:App", "Failed to launch package: ${app.packageName}.", e)
            LaunchResult.Error(e)
        }
    }

    override suspend fun openAppInfo(app: AppInfo): LaunchResult {
        return try {
            val componentName = ComponentName(app.packageName, app.componentClassName)
            val userHandle = userManager.getUserForSerialNumber(app.userHandleNumber)
                ?: return LaunchResult.NoLaunchIntent

            launcherApps.startAppDetailsActivity(
                componentName,
                userHandle,
                null,
                null
            )
            LaunchResult.Success
        } catch (e: Exception) {
            logger.e("$TAG:App", "Failed to open app info for ${app.packageName}.", e)
            LaunchResult.Error(e)
        }
    }

    override suspend fun uninstallApp(app: AppInfo): LaunchResult {
        return try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = "package:${app.packageName}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            LaunchResult.Success
        } catch (e: Exception) {
            logger.e("$TAG:App", "Failed to launch uninstall for ${app.packageName}.", e)
            LaunchResult.Error(e)
        }
    }

    override suspend fun launchShortcut(shortcut: AppShortcut): LaunchResult {
        return try {
            val userHandle = userManager.getUserForSerialNumber(shortcut.app.userHandleNumber)
                ?: return LaunchResult.NoLaunchIntent

            launcherApps.startShortcut(
                shortcut.app.packageName,
                shortcut.shortcutId,
                null,
                null,
                userHandle
            )
            LaunchResult.Success
        } catch (e: Exception) {
            logger.e("$TAG:Shortcut", "Failed to launch shortcut ${shortcut.shortcutId}.", e)
            LaunchResult.Error(e)
        }
    }

    override suspend fun openAlarm(): LaunchResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (intent.resolveActivity(context.packageManager) == null) {
                logger.w("$TAG:Alarm", "No alarm app available.")
                LaunchResult.NoLaunchIntent
            } else {
                context.startActivity(intent)
                LaunchResult.Success
            }
        } catch (e: Exception) {
            logger.e("$TAG:Alarm", "Failed to open alarm app.", e)
            LaunchResult.Error(e)
        }
    }

    override suspend fun openCalendar(): LaunchResult {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_CALENDAR)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (intent.resolveActivity(context.packageManager) == null) {
                logger.w("$TAG:Calendar", "No calendar app available.")
                LaunchResult.NoLaunchIntent
            } else {
                context.startActivity(intent)
                LaunchResult.Success
            }
        } catch (e: Exception) {
            logger.e("$TAG:Calendar", "Failed to open calendar app.", e)
            LaunchResult.Error(e)
        }
    }
}
