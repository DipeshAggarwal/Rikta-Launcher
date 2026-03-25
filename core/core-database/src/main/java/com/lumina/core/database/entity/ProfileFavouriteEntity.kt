package com.lumina.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import com.lumina.core.model.FavouriteItemType

private const val VERSION = 1

@Entity(
    tableName = "profile_favourites",
    primaryKeys = ["profileId", "itemId"],
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProfileFavouriteEntity(
    val profileId: String,
    val itemId: String,
    val itemType: FavouriteItemType,
    val favouriteOrder: Int,

    val version: Int = VERSION
)
