package com.lumina.core.android

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.AlarmManager
import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

private const val ANDROID_STORAGE_DELIMITER = ":"

@Singleton
class PlatformCapabilityChecker @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    // Runtime Permissions
    fun hasNetworkStatePermission(): Boolean {
        return hasPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    }

    fun hasFineLocationPermission(): Boolean {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun hasBackgroundLocationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    fun hasBluetoothConnectPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
    }

    // Trigger checks
    fun canMonitorWifiTriggers(): Boolean {
        return hasNetworkStatePermission() && hasFineLocationPermission()
    }

    fun canMonitorBluetoothTriggers(): Boolean {
        return hasBluetoothConnectPermission()
    }

    fun canMonitorLocationTriggers(): Boolean {
        return hasBackgroundLocationPermission() && hasFineLocationPermission()
    }

    fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    // System Permission checks
    fun isAccessibilityServiceEnabled(serviceClass: Class<out AccessibilityService>): Boolean {
        val expectedComponent = ComponentName(context, serviceClass).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices
            .split(ANDROID_STORAGE_DELIMITER)
            .any { it.equals(expectedComponent, ignoreCase = true) }
    }

    fun hasNotificationListenerAccess(): Boolean {
        return NotificationManagerCompat
            .getEnabledListenerPackages(context)
            .contains(context.packageName)
    }

    fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }

        val resolvedInfo = context.packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        ) ?: return false

        return resolvedInfo.activityInfo.packageName == context.packageName
    }

    // Authentication checks
    fun hasDeviceCredential(): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isDeviceSecure
    }

    fun canUseBiometricAuth(): Boolean {
        val biometricManager = BiometricManager.from(context)

        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun canAuthenticateSecurely(): Boolean {
        val biometricManager = BiometricManager.from(context)

        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
