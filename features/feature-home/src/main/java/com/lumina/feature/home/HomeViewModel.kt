package com.lumina.feature.home

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.common.FlowDefaults.WhileSubscribedTimeoutMillis
import com.lumina.core.logging.Logger
import com.lumina.domain.apps.AppInfo
import com.lumina.domain.apps.AppProfile
import com.lumina.domain.apps.FavouriteAppsRepository
import com.lumina.domain.apps.HiddenAppsRepository
import com.lumina.domain.apps.InstalledAppsRepository
import com.lumina.domain.search.AppSearchEngine
import com.lumina.domain.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    hiddenAppsRepository: HiddenAppsRepository,
    favouriteAppsRepository: FavouriteAppsRepository,
    settingsRepository: SettingsRepository,
    installedAppsRepository: InstalledAppsRepository,
    appSearchEngine: AppSearchEngine,
    private val logger: Logger
): ViewModel() {
    private val TAG = this::class.java.simpleName

    // SharedFlow because one time event.
    // Only expose the listener to the rest of the app.
    private val _navigateHomeEvent = MutableSharedFlow<Unit>(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )
    val navigateHomeEvent = _navigateHomeEvent.asSharedFlow()

    // These variables control if search is expanded.
    private val _isSearchExpanded = MutableStateFlow(false)
    val isSearchExpanded = _isSearchExpanded.asStateFlow()

    // These variables control the bottom drawer.
    private val _bottomSheetState = MutableStateFlow<BottomSheetState>(BottomSheetState.None)
    val bottomSheetState = _bottomSheetState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    // .Eagerly is used so that startup happens at creation time.
    // This improves animation and loading experience.
    private val installedApps: StateFlow<List<AppInfo>> = installedAppsRepository.installedApps()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    private val hiddenPackagesSet: StateFlow<Set<String>> = hiddenAppsRepository.allHiddenApps()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptySet()
        )

    private val favouritePackages: StateFlow<List<String>> = favouriteAppsRepository.allFavouriteApps()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    private val showHiddenAppsInSearch: StateFlow<Boolean> = settingsRepository.showHiddenAppsInSearch()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false
        )

    // Create a map for faster lookup.
    private val installedAppsMap: StateFlow<Map<String, AppInfo>> = installedApps
        .map { list -> list.associateBy { it.packageName } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMillis),
            emptyMap()
        )

    private val favouriteApps: StateFlow<List<AppInfo>> = combine(
        favouritePackages, installedAppsMap
    ) { pkg, map ->
        pkg.mapNotNull { map[it] }
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMillis),
            emptyList()
        )

    val screenTimePageVisible: StateFlow<Boolean> = settingsRepository.screenTimePageInHome()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false
        )

    // For future ref: Combine can only take a maximum of five Flows.
    // This is recomputed anytime any of the source changes.
    val homeUiState: StateFlow<HomeUiState> = combine(
        installedApps,
        hiddenPackagesSet,
        favouriteApps,
        searchQuery,
        showHiddenAppsInSearch
    ) { apps, hiddenApps, favApps, query, showInSearch ->
        val favouriteSet = favApps.map { it.packageName }.toSet()
        val isSearching = query.isNotBlank()
        val showHiddenAppsWhileSearching = showInSearch && isSearching
        val visibleApps = if (showHiddenAppsWhileSearching) {
            apps
        } else {
            apps.filterNot { it.packageName in hiddenApps }
        }

        val finalAppsList = if (isSearching) {
            appSearchEngine.search(visibleApps, query, favouriteSet)
        } else {
            visibleApps.sortedBy { it.displayName.lowercase() }
        }

        HomeUiState.Ready(
            favApps,
            finalAppsList,
            query,
            isSearching
        )
    }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            HomeUiState.Loading
        )

    fun requestToGoHome() {
        viewModelScope.launch {
            _navigateHomeEvent.emit(Unit)
        }
    }

    fun setSearchExpanded(expanded: Boolean) {
        _isSearchExpanded.value = expanded
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onAppLongPressed(app: AppInfo, profile: AppProfile = AppProfile.Standard) {
        _bottomSheetState.value = BottomSheetState.AppOptions(SelectedApp(app, profile))
    }

    fun onBottomSheetDismissed() {
        _bottomSheetState.value = BottomSheetState.None
    }

    fun openAlarm(context: Context) {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            // Checking for a null component name is more reliable in some API versions.
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                logger.w("$TAG.Alarm", "No alarm app available")
            }
        } catch (e: Exception) {
            logger.e("$TAG + :Alarm", "Failed to open alarm app.", e)
        }
    }

    fun openCalendar(context: Context) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_CALENDAR)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                logger.w("$TAG.Alarm", "No calendar app available")
            }
        } catch (e: Exception) {
            logger.e("$TAG + :Calendar", "Failed to open calendar app.", e)
        }
    }
}
