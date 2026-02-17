package com.lumina.domain.apps.usecase

import com.lumina.domain.apps.HiddenAppsRepository
import com.lumina.domain.apps.InstalledAppsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Use case to synchronize the hidden apps database with the system's actual installed apps.
 * It ensures that if an app is uninstalled from the device, it is also removed from the hidden apps
 * list, preventing "ghost" entries in the database.
 */
class CleanUpUninstalledHiddenAppsUseCase @Inject constructor(
    private val hiddenAppsRepository: HiddenAppsRepository,
    private val installedAppsRepository: InstalledAppsRepository
) {
    /**
     * Executes the cleanup logic.
     * Compares the current set of system-installed packages against the hidden list.
     */
    suspend operator fun invoke() {
        // Retrieve currently installed package names as a Set for O(1) lookup
        val installedPackages = installedAppsRepository.installedApps()
            .first()
            .map { it.packageName }
            .toSet()
        val hiddenPackages = hiddenAppsRepository.allHiddenApps().first()
        val cleanedHiddenPackages = hiddenPackages.filter { it in installedPackages }

        // Update the repository only if a change occurred (avoids unnecessary DataStore writes)
        if (hiddenPackages != cleanedHiddenPackages) {
            hiddenAppsRepository.setHiddenApps(cleanedHiddenPackages)
        }
    }
}
