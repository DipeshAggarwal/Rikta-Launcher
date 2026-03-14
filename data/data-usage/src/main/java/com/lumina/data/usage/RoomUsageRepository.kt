package com.lumina.data.usage

import com.lumina.core.database.dao.AppUsageDao
import com.lumina.core.model.AppBasicData
import com.lumina.domain.usage.UsageRepository
import com.lumina.domain.usage.model.AppUsageHourlyBreakdown
import com.lumina.domain.usage.model.AppUsageSummary
import com.lumina.domain.usage.model.CategoryUsageSummary
import com.lumina.domain.usage.model.UsageTimeRange
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class RoomUsageRepository @Inject constructor(
    private val appUsageDao: AppUsageDao
) : UsageRepository {
    override fun getUsageForProfile(
        profileID: String,
        range: UsageTimeRange
    ): Flow<List<AppUsageSummary>> {
        val (startMs, endMs) = range.toEpochBounds()

        return appUsageDao.getUsageStatsForProfile(profileID, startMs, endMs).map { rows ->
            rows.map { row ->
                AppUsageSummary(
                    app = AppBasicData(row.packageName, row.userHandleNumber),
                    totalMs = row.totalMs,
                    maxSessionMs = row.maxMs,
                    sessionCount = row.sessionCount
                )
            }
        }
    }

    override fun getTotalUsageAllProfiles(range: UsageTimeRange): Flow<List<AppUsageSummary>> {
        val (startMs, endMs) = range.toEpochBounds()

        return appUsageDao.getTotalUsageAllProfiles(startMs, endMs).map { rows ->
            rows.map { row ->
                AppUsageSummary(
                    app = AppBasicData(row.packageName, row.userHandleNumber),
                    totalMs = row.totalMs,
                    maxSessionMs = row.maxMs,
                    sessionCount = row.sessionCount
                )
            }
        }
    }

    override fun getCategoryUsage(
        profileId: String,
        range: UsageTimeRange
    ): Flow<List<CategoryUsageSummary>> {
        val (startMs, endMs) = range.toEpochBounds()

        return appUsageDao.getCategoryUsage(profileId, startMs, endMs).map { rows ->
            rows.map { row ->
                CategoryUsageSummary(
                    category = row.category,
                    customLabel = row.customCategoryName,
                    totalMs = row.totalMs
                )
            }
        }
    }

    override suspend fun getSessionStats(
        packageName: String,
        userHandleNumber: Long,
        profileId: String,
        range: UsageTimeRange
    ): AppUsageSummary? {
        val (startMs, endMs) = range.toEpochBounds()
        val row = appUsageDao.getAppSessionStats(
            packageName, userHandleNumber, profileId, startMs, endMs
        ) ?: return null

        return AppUsageSummary(
            app = AppBasicData(packageName, userHandleNumber),
            totalMs = row.totalMs,
            maxSessionMs = row.maxMs,
            sessionCount = row.sessionCount
        )
    }

    override suspend fun getHourlyUsage(
        packageName: String,
        userHandleNumber: Long,
        profileId: String,
        range: UsageTimeRange
    ): List<AppUsageHourlyBreakdown> {
        val (startMs, endMs) = range.toEpochBounds()

        return appUsageDao.getAppHourlyUsage(
            packageName, userHandleNumber, profileId, startMs, endMs
        ).map { row ->
            AppUsageHourlyBreakdown(
                hourOfDay = row.hourOfDay,
                totalMs = row.totalMs
            )
        }
    }
}
