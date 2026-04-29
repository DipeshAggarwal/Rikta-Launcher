package com.lumina.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lumina.core.model.ShortcutType

private const val VERSION = 1

@Entity(tableName = "shortcuts")
data class ShortcutEntity(
    @PrimaryKey val id: String,
    val label: String,
    val type: ShortcutType,
    val userHandleNumber: Long,

    val shortcutPackage: String?,
    val shortcutId: String?,

    val url: String?,
    val targetPackage: String?,

    val version: Int = VERSION
)
