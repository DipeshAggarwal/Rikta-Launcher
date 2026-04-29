package com.lumina.feature.home.model

import com.lumina.core.model.LauncherItem

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Ready(
        val favourites: List<LauncherItem>,
        val apps: List<LauncherItem.App>,
        val searchQuery: String,
        val isSearching: Boolean
    ) : HomeUiState
}
