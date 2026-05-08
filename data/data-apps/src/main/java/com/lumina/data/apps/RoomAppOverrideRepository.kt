package com.lumina.data.apps

import androidx.room.withTransaction
import com.lumina.core.database.LuminaDatabase
import com.lumina.core.database.dao.AppOverrideDao
import com.lumina.core.database.entity.AppOverrideEntity
import com.lumina.core.model.AppCategory
import com.lumina.core.model.AppOverride
import com.lumina.domain.apps.AppOverrideRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject
import kotlin.collections.map

class RoomAppOverrideRepository @Inject constructor(
    private val database: LuminaDatabase,
    private val appOverrideDao: AppOverrideDao
) : AppOverrideRepository {
    override val allOverrides: Flow<List<AppOverride>>
        get() = appOverrideDao.getAll().map { apps ->
            apps.map { overrideData ->
                AppOverride(
                    packageName = overrideData.packageName,
                    userHandleNumber = overrideData.userHandleNumber,
                    categoryOverride = overrideData.categoryOverride,
                    customCategoryName = overrideData.customCategoryName,
                    customDisplayName = overrideData.customDisplayName
                )
            }
        }

    override suspend fun get(
        packageName: String,
        userHandleNumber: Long
    ): AppOverride? {
        val overrideData = appOverrideDao.get(packageName, userHandleNumber) ?: return null

        return AppOverride(
            packageName = overrideData.packageName,
            userHandleNumber = overrideData.userHandleNumber,
            categoryOverride = overrideData.categoryOverride,
            customCategoryName = overrideData.customCategoryName,
            customDisplayName = overrideData.customDisplayName
        )
    }

    override suspend fun setDisplayName(
        packageName: String,
        userHandleNumber: Long,
        displayName: String?
    ) = database.withTransaction {
        val existing = appOverrideDao.get(packageName, userHandleNumber)

        if (existing != null) {
            appOverrideDao.updateDisplayName(packageName, userHandleNumber, displayName)
        } else {
            appOverrideDao.save(
                AppOverrideEntity(
                    packageName = packageName,
                    userHandleNumber = userHandleNumber,
                    customDisplayName = displayName
                )
            )
        }
    }

    override suspend fun setCategory(
        packageName: String,
        userHandleNumber: Long,
        category: AppCategory?,
        customCategoryName: String?
    ) = database.withTransaction {
        val existing = appOverrideDao.get(packageName, userHandleNumber)

        if (existing != null) {
            appOverrideDao.updateCategoryName(packageName, userHandleNumber, category, customCategoryName)
        } else {
            appOverrideDao.save(
                AppOverrideEntity(
                    packageName = packageName,
                    userHandleNumber = userHandleNumber,
                    categoryOverride = category,
                    customCategoryName = customCategoryName
                )
            )
        }
    }

    override suspend fun delete(packageName: String, userHandleNumber: Long) {
        appOverrideDao.delete(packageName, userHandleNumber)
    }

}