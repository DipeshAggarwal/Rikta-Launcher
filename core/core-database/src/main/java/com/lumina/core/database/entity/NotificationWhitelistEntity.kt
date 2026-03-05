package com.lumina.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "profile_notification_whitelist",
    primaryKeys = ["profileId", "packageName", "userHandleNumber"],
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]

)
data class NotificationWhitelistEntity(
    val profileId: String,
    val packageName: String,
    val userHandleNumber: Long
)
