package com.lumina.core.model

/**
 * Domain model representing a simplified version of an Android application.
 * This is used throughout the UI layer to display app lists without exposing heavy Android classes.
 */
data class AppInfo (
    val packageName: String,
    val componentClassName: String,
    val userHandleNumber: Long,
    val displayName: String,
    val category: AppCategory,
    val customCategoryName: String? = null
)
