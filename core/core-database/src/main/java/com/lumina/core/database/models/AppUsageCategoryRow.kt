package com.lumina.core.database.models

import com.lumina.core.model.AppCategory

data class AppUsageCategoryRow(
    val category: AppCategory,
    val customCategoryName: String?,
    val totalMs: Long
)
