package com.lumina.domain.apps

import com.lumina.core.model.AppCategory
import com.lumina.core.model.AppOverride
import kotlinx.coroutines.flow.Flow

interface AppOverrideRepository {
    val allOverrides: Flow<List<AppOverride>>
    suspend fun get(packageName: String, userHandleNumber: Long): AppOverride?
    suspend fun setDisplayName(packageName: String, userHandleNumber: Long, displayName: String?)
    suspend fun setCategory(
        packageName: String,
        userHandleNumber: Long,
        category: AppCategory?,
        customCategoryName: String? = null
    )
    suspend fun delete(packageName: String, userHandleNumber: Long)
}
