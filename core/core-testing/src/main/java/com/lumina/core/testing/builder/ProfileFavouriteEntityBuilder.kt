package com.lumina.core.testing.builder

import com.lumina.core.database.entity.ProfileFavouriteEntity
import com.lumina.core.model.FavouriteItemType

object ProfileFavouriteEntityBuilder {
    fun build(
        profileId: String = "profile_test_1",
        itemId: String = "com.example.app::0",
        itemType: FavouriteItemType = FavouriteItemType.APP,
        favouriteOrder: Int = 128
    ) = ProfileFavouriteEntity(
        profileId = profileId,
        itemId = itemId,
        itemType = itemType,
        favouriteOrder = favouriteOrder
    )
}
