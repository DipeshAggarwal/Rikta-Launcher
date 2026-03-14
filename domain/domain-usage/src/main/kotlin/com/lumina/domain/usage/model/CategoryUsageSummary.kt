package com.lumina.domain.usage.model

import com.lumina.core.model.AppCategory

data class CategoryUsageSummary(
    val category: AppCategory,
    val customLabel: String?,
    val totalMs: Long
)
