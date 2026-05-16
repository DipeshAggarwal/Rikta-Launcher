package com.lumina.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.lumina.core.database.entity.ProfileFavouriteEntity
import com.lumina.core.model.FavouriteItemType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Dao
interface ProfileFavouriteDao {

    @Query("""
        SELECT * FROM profile_favourites
        WHERE profileId = :profileId AND itemId = :itemId
    """)
    suspend fun get(profileId: String, itemId: String): ProfileFavouriteEntity?

    @Query("""
        SELECT * FROM profile_favourites
        WHERE profileId = :profileId
        ORDER BY favouriteOrder ASC
    """)
    fun getAll(profileId: String): Flow<List<ProfileFavouriteEntity>>

    @Query("""
        SELECT MAX(favouriteOrder) FROM profile_favourites
        WHERE profileId = :profileId
    """)
    suspend fun getMaxOrder(profileId: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(favourite: ProfileFavouriteEntity)

    @Query("""
        DELETE FROM profile_favourites
        WHERE profileId = :profileId AND itemId = :itemId
    """)
    suspend fun delete(profileId: String, itemId: String)

    @Query("UPDATE profile_favourites SET favouriteOrder = :favouriteOrder " +
            "WHERE profileId = :profileId AND itemId = :itemId"
    )
    suspend fun update(profileId: String, itemId: String, favouriteOrder: Int)

    @Transaction
    suspend fun toggle(
        profileId: String,
        itemId: String,
        itemType: FavouriteItemType,
        gap: Int
    ) {
        val existingFavourite = get(profileId, itemId)

        if (existingFavourite != null) {
            delete(profileId, itemId)
        } else {
            val max = getMaxOrder(profileId) ?: 0
            save(ProfileFavouriteEntity(profileId, itemId, itemType, max + gap))
        }
    }

    @Transaction
    suspend fun rebalance(profileId: String, gap: Int) {
        val currentFavourites = getAll(profileId).first()
        currentFavourites.forEachIndexed { index, item ->
            update(profileId, item.itemId, (index + 1) * gap)
        }
    }
}
