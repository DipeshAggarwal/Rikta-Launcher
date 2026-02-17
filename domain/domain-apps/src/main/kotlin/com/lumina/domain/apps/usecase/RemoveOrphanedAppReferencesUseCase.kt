package com.lumina.domain.apps.usecase

import com.lumina.domain.apps.FavouriteAppsRepository
import com.lumina.domain.apps.HiddenAppsRepository
import com.lumina.domain.apps.InstalledAppsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Use case to validate all internal apps datastores with the system's actual installed apps.
 * It ensures that if an app is uninstalled from the device, it is also removed from the all internal
 * apps list, preventing "ghost" entries in the database.
 *
 * In future, this would most probably need one more abstraction to stop it becoming a master usercase.
 */
class RemoveOrphanedAppReferencesUseCase @Inject constructor(
    private val hiddenAppsRepository: HiddenAppsRepository,
    private val favouriteAppsRepository: FavouriteAppsRepository,
    private val installedAppsRepository: InstalledAppsRepository
) {
    /**
     * Executes the cleanup logic.
     * Compares the current set of system-installed packages against the hidden list.
     */
    suspend operator fun invoke() {
        // Retrieve currently installed package names as a Set for O(1) lookup.
        val installedPackages = installedAppsRepository.installedApps()
            .first()
            .map { it.packageName }
            .toSet()

        cleanHiddenApps(installedPackages)
        cleanFavouriteApps(installedPackages)
    }

    private suspend fun cleanHiddenApps(installedPackages: Set<String>) {
        val hiddenPackages = hiddenAppsRepository.allHiddenApps().first()
        val cleanedHiddenPackages = hiddenPackages.filter { it in installedPackages }

        // Update the repository only if a change occurred (avoids unnecessary DataStore writes).
        if (hiddenPackages != cleanedHiddenPackages) {
            hiddenAppsRepository.setHiddenApps(cleanedHiddenPackages)
        }
    }

    private suspend fun cleanFavouriteApps(installedPackages: Set<String>) {
        val favouritePackages = favouriteAppsRepository.allFavouriteApps().first()
        val cleanedFavouritePackages = favouritePackages.filter { it in installedPackages }

        // Update the repository only if a change occurred (avoids unnecessary DataStore writes).
        if (favouritePackages != cleanedFavouritePackages) {
            favouriteAppsRepository.setFavouriteApps(cleanedFavouritePackages)
        }
    }
}
