package com.lumina.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

private const val VERSION = 1

@Entity(
    tableName = "profile_app_mapping",
    primaryKeys = ["profileId", "packageName", "userHandleNumber"],
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["packageName", "userHandleNumber"])]
)

data class ProfileAppCrossRef(
    val profileId: String,
    val packageName: String,
    val userHandleNumber: Long,

    val favouriteOrder: Int? = null,
    val showCountdown: Boolean = false,
    val recommendedUsageMinutes: Int? = null,

    val version: Int = VERSION
)
