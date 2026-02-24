package com.lumina.feature.home

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
import com.lumina.domain.settings.AppListSettings
import com.lumina.domain.settings.HomeSettings
import com.lumina.domain.settings.SearchSettings
import com.lumina.domain.settings.SettingsRepository
import com.lumina.domain.system.IntentLauncher
import com.lumina.domain.system.LaunchResult
import com.lumina.domain.system.StatusBarController
import com.lumina.feature.home.model.BottomSheetState
import com.lumina.feature.home.model.HomeUiState
import com.lumina.feature.home.model.SelectedApp
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
    private val intentLauncher: IntentLauncher,
    private val statusBarController: StatusBarController,
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

    private val _appToOpen = MutableSharedFlow<AppInfo>(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )
    val appToOpen = _appToOpen.asSharedFlow()

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

    val homeSettings: StateFlow<HomeSettings> = settingsRepository.homeSettings
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            HomeSettings()
        )

    val appListSettings: StateFlow<AppListSettings> = settingsRepository.appListSettings
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppListSettings()
        )

    val searchSettings: StateFlow<SearchSettings> = settingsRepository.searchSettings
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            SearchSettings()
        )

    // For future ref: Combine can only take a maximum of five Flows.
    // This is recomputed anytime any of the source changes.
    val homeUiState: StateFlow<HomeUiState> = combine(
        installedApps,
        hiddenPackagesSet,
        favouriteApps,
        searchQuery,
        searchSettings
    ) { apps, hiddenApps, favApps, query, searchPrefs ->
        val favouriteSet = favApps.map { it.packageName }.toSet()
        val isSearching = query.isNotBlank()
        val showHiddenAppsWhileSearching = searchPrefs.showHiddenAppsInSearch && isSearching
        val visibleApps = if (showHiddenAppsWhileSearching) {
            apps
        } else {
            apps.filterNot { it.packageName in hiddenApps }
        }
        val favForBoosting = if (searchPrefs.favouriteBoostInSearch) favouriteSet else emptySet()

        val finalAppsList = if (isSearching) {
            appSearchEngine.search(visibleApps, query, favForBoosting)
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

    private fun handleLaunchResult(result: LaunchResult) {
        when (result) {
            is LaunchResult.Success -> {
                setSearchExpanded(false)
                onSearchQueryChanged("")
                requestToGoHome()
            }
            is LaunchResult.NoLaunchIntent -> {}
            is LaunchResult.Error -> {}
        }
    }

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

    fun onSearchDone() {
        val state = homeUiState.value as? HomeUiState.Ready ?: return
        val firstApp = state.apps.firstOrNull() ?: return
        onAppOpened(firstApp)
    }

    fun onAppOpened(app: AppInfo) {
        viewModelScope.launch {
            val result = intentLauncher.openApp(app)
            handleLaunchResult(result)
        }
    }

    fun onAppLongPressed(app: AppInfo, profile: AppProfile = AppProfile.Standard) {
        _bottomSheetState.value = BottomSheetState.AppOptions(SelectedApp(app, profile))
    }

    fun onBottomSheetDismissed() {
        _bottomSheetState.value = BottomSheetState.None
    }

    fun onExpandNotificationShade() {
        statusBarController.expandNotificationShade()
    }

    fun openAlarm() {
        viewModelScope.launch {
            val result = intentLauncher.openAlarm()
            handleLaunchResult(result)
        }
    }

    fun openCalendar() {
        viewModelScope.launch {
            val result = intentLauncher.openCalendar()
            handleLaunchResult(result)
        }
    }
}
