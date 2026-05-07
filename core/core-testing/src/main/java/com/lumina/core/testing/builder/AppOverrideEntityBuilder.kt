package com.lumina.core.testing.builder

import com.lumina.core.database.entity.AppOverrideEntity
import com.lumina.core.model.AppCategory
import kotlin.uuid.Uuid

object AppOverrideEntityBuilder {
    fun build(
        packageName: String = "com.example.app",
        userHandleNumber: Long = 0L,
        categoryOverride: AppCategory? = AppCategory.PRODUCTIVITY,
        customCategoryName: String? = null,
        customDisplayName: String? = "Package Name",
    ) = AppOverrideEntity(
        packageName = packageName,
        userHandleNumber = userHandleNumber,
        categoryOverride = categoryOverride,
        customCategoryName = customCategoryName,
        customDisplayName = customDisplayName
    )
}
