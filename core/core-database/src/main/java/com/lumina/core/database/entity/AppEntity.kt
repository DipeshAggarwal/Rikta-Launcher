package com.lumina.core.database.entity

import androidx.room.Entity
import com.lumina.core.model.AppCategory

private const val VERSION = 1

@Entity(
    tableName = "apps",
    primaryKeys = ["packageName", "userHandleNumber"]
)
data class AppEntity(
    val packageName: String,
    val userHandleNumber: Long,

    val category: AppCategory,
    val categoryOverride: AppCategory? = null,
    val customCategoryName: String? = null,

    val customDisplayName: String? = null,

    val version: Int = VERSION
)
