package com.lumina.rikta.startup

import com.lumina.data.apps.installed.AppChangeEvent
import com.lumina.data.apps.installed.InstalledAppsMonitor
import com.lumina.domain.coordination.usecase.RemoveOrphanedAppReferencesUseCase
import com.lumina.core.android.di.ApplicationScope
import com.lumina.domain.profiles.usecase.SyncActiveProfileUseCase
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * Global coordinator responsible for maintaining data integrity between the Android system and the
 * app's internal database.
 * It listens for package installs/uninstalls and triggers cleanup tasks to ensure the hidden apps
 * list doesn't contain "ghost" entries for uninstalled packages.
 */
@Singleton
class DataIntegrityCoordinator @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val removeOrphanedAppReferencesUseCase: RemoveOrphanedAppReferencesUseCase,
    private val syncActiveProfileUseCase: SyncActiveProfileUseCase,
    installedAppsMonitor: InstalledAppsMonitor
) {
    init {
        applicationScope.launch {
            syncActiveProfileUseCase()

            installedAppsMonitor.appChanges()
                // .onStart ensures we scrub the DB immediately on app launch, catching uninstalls
                // that happened while the launcher was stopped.
                .onStart { emit(AppChangeEvent.Initial) }
                .collect {
                    removeOrphanedAppReferencesUseCase()
                }
        }
    }
}
