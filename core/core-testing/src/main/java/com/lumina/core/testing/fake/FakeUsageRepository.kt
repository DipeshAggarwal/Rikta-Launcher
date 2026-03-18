package com.lumina.core.testing.fake

import com.lumina.domain.usage.UsageRepository
import com.lumina.domain.usage.model.AppUsageHourlyBreakdown
import com.lumina.domain.usage.model.AppUsageSummary
import com.lumina.domain.usage.model.CategoryUsageSummary
import com.lumina.domain.usage.model.UsageTimeRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUsageRepository : UsageRepository {
    val sessionStatsSummary: AppUsageSummary? = null
    val profileUsageSummary: List<AppUsageSummary> = emptyList()
    val categoryUsageSummary: List<CategoryUsageSummary> = emptyList()
    val hourlyUsageBreakdown: List<AppUsageHourlyBreakdown> = emptyList()

    val getSessionStatsCall = mutableListOf<Triple<String, Long, String>>()

    override fun getUsageForProfile(
        profileID: String,
        range: UsageTimeRange
    ): Flow<List<AppUsageSummary>> {
        return MutableStateFlow(profileUsageSummary)
    }

    override fun getTotalUsageAllProfiles(range: UsageTimeRange): Flow<List<AppUsageSummary>> {
        return MutableStateFlow(profileUsageSummary)
    }

    override fun getCategoryUsage(
        profileId: String,
        range: UsageTimeRange
    ): Flow<List<CategoryUsageSummary>> {
        return MutableStateFlow(categoryUsageSummary)
    }

    override suspend fun getSessionStats(
        packageName: String,
        userHandleNumber: Long,
        profileId: String,
        range: UsageTimeRange
    ): AppUsageSummary? {
        getSessionStatsCall.add(Triple(packageName, userHandleNumber, profileId))
        return sessionStatsSummary
    }

    override suspend fun getHourlyUsage(
        packageName: String,
        userHandleNumber: Long,
        profileId: String,
        range: UsageTimeRange
    ): List<AppUsageHourlyBreakdown> {
        return hourlyUsageBreakdown
    }
}
