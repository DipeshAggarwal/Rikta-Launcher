package com.lumina.domain.usage.model

import com.lumina.core.model.AppBasicData

data class AppUsageSummary(
    val app: AppBasicData,
    val totalMs: Long,
    val maxSessionMs: Long,
    val sessionCount: Int,
) {
    val avgSessionMs: Long
        get() = if (sessionCount > 0) totalMs / sessionCount else 0
}
