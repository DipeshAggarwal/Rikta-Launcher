package com.lumina.domain.usage

import com.lumina.domain.usage.model.AppUsageHourlyBreakdown
import com.lumina.domain.usage.model.AppUsageSummary
import com.lumina.domain.usage.model.CategoryUsageSummary
import com.lumina.domain.usage.model.UsageTimeRange
import kotlinx.coroutines.flow.Flow

interface UsageRepository {
    fun getUsageForProfile(profileID: String, range: UsageTimeRange): Flow<List<AppUsageSummary>>
    fun getTotalUsageAllProfiles(range: UsageTimeRange): Flow<List<AppUsageSummary>>
    fun getCategoryUsage(profileId: String, range: UsageTimeRange): Flow<List<CategoryUsageSummary>>

    suspend fun getSessionStats(
        packageName: String,
        userHandleNumber: Long,
        profileId: String,
        range: UsageTimeRange
    ): AppUsageSummary?
    suspend fun getHourlyUsage(
        packageName: String,
        userHandleNumber: Long,
        profileId: String,
        range: UsageTimeRange
    ): List<AppUsageHourlyBreakdown>
}
