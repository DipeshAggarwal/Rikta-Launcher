package com.lumina.data.profiles

import com.lumina.core.database.dao.ProfileFavouriteDao
import com.lumina.core.logging.Logger
import com.lumina.core.model.FavouriteItem
import com.lumina.core.model.FavouriteItemType
import com.lumina.domain.profiles.ProfileFavouriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject

private const val FAVOURITE_GAP = 128

class RoomProfileFavouriteRepository @Inject constructor(
    private val profileFavouriteDao: ProfileFavouriteDao,
    private val logger: Logger
) : ProfileFavouriteRepository {
    private val TAG = this::class.java.simpleName

    override fun getAll(profileId: String): Flow<List<FavouriteItem>> {
        return profileFavouriteDao.getAll(profileId).map { apps ->
            apps.map { app ->
                FavouriteItem(
                    itemId = app.itemId,
                    itemType = app.itemType,
                    favouriteOrder = app.favouriteOrder
                )
            }
        }
    }

    override suspend fun update(
        profileId: String,
        itemId: String,
        previous: Int?,
        next: Int?
    ) {
        val newOrder = when {
            previous == null && next != null -> next / 2
            next == null && previous != null -> previous + FAVOURITE_GAP
            previous != null && next != null -> (previous + next) / 2
            else -> FAVOURITE_GAP
        }

        if (newOrder == previous || newOrder == next) {
            profileFavouriteDao.update(profileId, itemId, newOrder)
            profileFavouriteDao.rebalance(profileId, FAVOURITE_GAP)
        } else {
            profileFavouriteDao.update(profileId, itemId, newOrder)
        }
    }

    override suspend fun toggle(
        profileId: String,
        itemId: String,
        itemType: FavouriteItemType
    ) {
        profileFavouriteDao.toggle(profileId, itemId, itemType, FAVOURITE_GAP)
    }
}
