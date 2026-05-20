package com.lumina.domain.profiles.manager

import com.lumina.core.model.AppAddedSource
import com.lumina.core.model.AppBasicData
import com.lumina.core.model.AppCategory
import com.lumina.core.model.AppInfo
import com.lumina.core.model.componentKey
import com.lumina.domain.apps.InstalledAppsRepository
import com.lumina.domain.profiles.ProfileRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class ProfileCategorySyncManager @Inject constructor(
    private val installedAppsRepository: InstalledAppsRepository,
    private val profileRepository: ProfileRepository
) {
    suspend fun observeAndSyncCategories() {
        var previousAppCategories: Map<String, AppCategory>? = null

        installedAppsRepository.apps.collect { currentApps ->
            val currentCategories = currentApps.associate { it.componentKey to it.category }

            if (previousAppCategories != null) {
                val evaluatingApp = currentApps.filter { app ->
                    val oldCategory = previousAppCategories!![app.componentKey]
                    oldCategory == null || oldCategory != app.category
                }

                if (evaluatingApp.isNotEmpty()) syncAllProfiles(evaluatingApp)
            }

            previousAppCategories = currentCategories
        }
    }

    suspend fun syncProfile(profileId: String) {
        val profile = profileRepository.getProfileById(profileId).first() ?: return
        val allProfileApps = installedAppsRepository.apps.first()
        val targetCategories = profile.settings.autoAddCategoryApps

        processAppsForProfile(
            profileId = profile.id,
            targetCategories = targetCategories,
            appsToEvaluate = allProfileApps
        )
    }

    private suspend fun syncAllProfiles(appsToEvaluate: List<AppInfo>) {
        val profiles = profileRepository.getAllProfiles().first()

        profiles.forEach { profile ->
            val targetCategories = profile.settings.autoAddCategoryApps
            if (targetCategories.isEmpty()) return@forEach

            processAppsForProfile(
                profileId = profile.id,
                targetCategories = targetCategories,
                appsToEvaluate = appsToEvaluate
            )
        }
    }

    private suspend fun processAppsForProfile(
        profileId: String,
        targetCategories: List<AppCategory>,
        appsToEvaluate: List<AppInfo>
    ) {
        val appsToAdd = appsToEvaluate
            .filter { it.category in targetCategories }
            .map { AppBasicData(it.packageName, it.userHandleNumber) }

        val appsToRemoveKey = appsToEvaluate
            .filter { it.category !in targetCategories }
            .map { it.componentKey }
            .toSet()

        if (appsToAdd.isNotEmpty()) {
            profileRepository.addAppsToProfile(
                profileId = profileId,
                apps = appsToAdd,
                addedBy = AppAddedSource.RULE
            )
        }

        if (appsToRemoveKey.isNotEmpty()) {
            profileRepository.removeAppsAddedByRules(
                profileId = profileId,
                installedKeys = appsToRemoveKey
            )
        }
    }
}