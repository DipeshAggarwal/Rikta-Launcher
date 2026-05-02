package com.lumina.feature.system

import android.os.UserManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.lumina.core.android.di.ApplicationScope
import com.lumina.core.logging.Logger
import com.lumina.core.model.AppBasicData
import com.lumina.domain.profiles.ProfileRepository
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class LuminaNotificationListener : NotificationListenerService() {
    @Inject lateinit var repository: ProfileRepository
    @Inject @ApplicationScope lateinit var scope: CoroutineScope
    @Inject lateinit var userManager: UserManager
    @Inject lateinit var logger: Logger

    @Volatile private var isFilteringEnabled = false
    @Volatile private var allowedApps: Set<AppBasicData> = emptySet()

    private val TAG = this::class.java.simpleName

    private var observerJob: Job? = null
    private var hasProcessedInitial = false

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeProfileRules(): Job {
        return repository.activeProfile
            .flatMapLatest { profile ->
                isFilteringEnabled = profile?.settings?.filterNotification == true

                if (isFilteringEnabled) {
                    if (profile != null) {
                        repository.getNotificationAllowedApps(profile.id)
                    } else {
                        flowOf(emptySet())
                    }
                } else {
                    flowOf(emptySet())
                }
            }
            .onEach { apps ->
                allowedApps = apps
                if (isFilteringEnabled && !hasProcessedInitial) {
                    hasProcessedInitial = true
                    activeNotifications?.forEach { onNotificationPosted(it) }
                }
            }
            .launchIn(scope)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()

        hasProcessedInitial = false
        observerJob = observeProfileRules()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        observerJob?.cancel()
        observerJob = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        if (!isFilteringEnabled) return

        val appIdentifier = AppBasicData(sbn.packageName, userManager.getSerialNumberForUser(sbn.user))

        if (!allowedApps.contains(appIdentifier)) {
            logger.d(TAG, "Blocked ${sbn.packageName}")
            cancelNotification(sbn.key)
        }
    }
}
