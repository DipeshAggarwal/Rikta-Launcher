package com.lumina.core.model

data class AppOverride (
    val packageName: String,
    val userHandleNumber: Long,
    val categoryOverride: AppCategory? = null,
    val customCategoryName: String? = null,
    val customDisplayName: String? = null
)
