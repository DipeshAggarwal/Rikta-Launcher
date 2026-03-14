package com.lumina.core.database.models

data class AppUsageStatsRow(
    val packageName: String,
    val userHandleNumber: Long,
    val maxMs: Long,
    val sessionCount: Int,
    val totalMs: Long
)
