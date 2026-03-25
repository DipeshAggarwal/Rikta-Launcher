package com.lumina.domain.profiles

import com.lumina.core.model.FavouriteItem
import com.lumina.core.model.FavouriteItemType
import kotlinx.coroutines.flow.Flow

interface ProfileFavouriteRepository {
    fun getAll(profileId: String): Flow<List<FavouriteItem>>
    suspend fun update(
        profileId: String,
        itemId: String,
        previous: Int,
        next: Int
    )
    suspend fun toggle(profileId: String, itemId: String, itemType: FavouriteItemType)
}
