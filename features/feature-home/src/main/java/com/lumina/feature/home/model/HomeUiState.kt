package com.lumina.feature.home.model

import com.lumina.domain.apps.AppInfo

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Ready(
        val favourites: List<AppInfo>,
        val apps: List<AppInfo>,
        val searchQuery: String,
        val isSearching: Boolean
    ) : HomeUiState
}
