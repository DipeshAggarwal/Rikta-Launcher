package com.lumina.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "profile_shortcut_mapping",
    primaryKeys = ["profileId", "shortcutId"],
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShortcutEntity::class,
            parentColumns = ["id"],
            childColumns = ["shortcutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId"), Index("shortcutId")]
)
data class ProfileShortcutCrossRef(
    val profileId: String,
    val shortcutId: String
)
