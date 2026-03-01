package com.lumina.data.apps.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lumina.core.logging.Logger
import com.lumina.domain.apps.FavouriteAppsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject

private const val DELIMITER = ","

/**
 * DataStore implementation of [FavouriteAppsRepository].
 * Since DataStore 'StringSet' does not preserve order, we store favorites as a single delimited
 * String to maintain the user's custom sorting.
 */
class DataStoreFavouriteAppsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val logger: Logger
) : FavouriteAppsRepository {
    private val FAVOURITE_APPS_KEY = stringPreferencesKey("favourite_apps")
    private val TAG = this::class.java.simpleName

    override val favouriteAppPackages: Flow<List<String>> = dataStore.data
        .map { prefs ->
            prefs[FAVOURITE_APPS_KEY]?.split(DELIMITER)?.filter { it.isNotEmpty() }
                ?: emptyList()
        }

    override suspend fun addFavouriteApp(packageName: String) {
        dataStore.edit { prefs ->
            val currentFavourites = prefs[FAVOURITE_APPS_KEY]?.split(DELIMITER)
                ?: emptyList()

            if (!currentFavourites.contains(packageName)) {
                val updatedFavourites = currentFavourites + packageName
                prefs[FAVOURITE_APPS_KEY] = updatedFavourites.joinToString(separator=DELIMITER)
            }
        }
    }

    override suspend fun removeFavouriteApp(packageName: String) {
        dataStore.edit { prefs ->
            val currentFavourites = prefs[FAVOURITE_APPS_KEY]?.split(DELIMITER)
                ?: return@edit

            if (currentFavourites.contains(packageName)) {
                val updatedFavourites = currentFavourites - packageName
                prefs[FAVOURITE_APPS_KEY] = updatedFavourites.joinToString(separator=DELIMITER)
            }
        }

    }

    override suspend fun setFavouriteApps(packageNames: List<String>) {
        dataStore.edit { prefs ->
            prefs[FAVOURITE_APPS_KEY] = packageNames.distinct().joinToString(separator=DELIMITER)
        }
    }

    override suspend fun reorderFavouriteApps(fromIndex: Int, toIndex: Int) {
        dataStore.edit { prefs ->
            val currentFavourites = prefs[FAVOURITE_APPS_KEY]?.split(DELIMITER)
                ?.toMutableList()
                ?: return@edit

            if (fromIndex in currentFavourites.indices && toIndex in currentFavourites.indices) {
                val selectedApp = currentFavourites.removeAt(fromIndex)
                currentFavourites.add(toIndex, selectedApp)
                prefs[FAVOURITE_APPS_KEY] = currentFavourites.joinToString(DELIMITER)
            } else {
                logger.w(TAG,
                    "Invalid indices: from=$fromIndex, to=$toIndex, size=${currentFavourites.size}"
                )
            }
        }
    }
}
