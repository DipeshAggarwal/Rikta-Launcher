package com.lumina.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.TypeConverter
import com.lumina.core.model.AppAddedSource

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
    indices = [
        Index("profileId"),
        Index(value = ["packageName", "userHandleNumber"])
    ]
)

data class ProfileAppCrossRef(
    val profileId: String,
    val packageName: String,
    val userHandleNumber: Long,

    val addedBy: AppAddedSource = AppAddedSource.USER,

    val showCountdown: Boolean = false,
    val recommendedUsageMinutes: Int? = null,

    val version: Int = VERSION
)

class ProfileAppCrossRefConverters {
    @TypeConverter
    fun toAppAddedSource(value: String): AppAddedSource = enumValueOf(value)

    @TypeConverter
    fun fromAppAddedSource(source: AppAddedSource): String = source.name
}
