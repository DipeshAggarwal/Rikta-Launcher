package com.lumina.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.common.FlowDefaults.WhileSubscribedTimeoutMillis
import com.lumina.core.logging.Logger
import com.lumina.domain.apps.AppInfo
import com.lumina.domain.apps.AppProfile
import com.lumina.domain.apps.AppShortcut
import com.lumina.domain.apps.AppShortcutRepository
import com.lumina.domain.apps.FavouriteAppsRepository
import com.lumina.domain.apps.HiddenAppsRepository
import com.lumina.domain.apps.InstalledAppsRepository
import com.lumina.domain.countdown.CountdownRepository
import com.lumina.domain.search.AppSearchEngine
import com.lumina.domain.settings.AppListSettings
import com.lumina.domain.settings.HomeSettings
import com.lumina.domain.settings.SearchSettings
import com.lumina.domain.settings.SettingsRepository
import com.lumina.domain.system.AppLaunchCoordinator
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
    private val hiddenAppsRepository: HiddenAppsRepository,
    private val favouriteAppsRepository: FavouriteAppsRepository,
    private val countdownRepository: CountdownRepository,
    settingsRepository: SettingsRepository,
    installedAppsRepository: InstalledAppsRepository,
    private val appSearchEngine: AppSearchEngine,
    private val intentLauncher: IntentLauncher,
    private val launchCoordinator: AppLaunchCoordinator,
    private val shortcutRepository: AppShortcutRepository,
    private val statusBarController: StatusBarController,
    private val logger: Logger
) : ViewModel() {
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

    private val hiddenPackagesSet: StateFlow<Set<String>> = hiddenAppsRepository.hiddenAppPackages
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptySet()
        )

    private val favouritePackages: StateFlow<List<String>> = favouriteAppsRepository.favouriteAppPackages
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    private val countdownPackages: StateFlow<Set<String>> = countdownRepository.countdownAppPackages
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptySet()
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
            }
            is LaunchResult.NoLaunchIntent -> {}
            is LaunchResult.Error -> {}
        }
    }

    private fun getVisibleAppsForSearch(searchPrefs: SearchSettings): List<AppInfo> {
        val apps = installedApps.value
        val hidden = hiddenPackagesSet.value

        return if (searchPrefs.showHiddenAppsInSearch) apps
        else apps.filterNot { it.packageName in hidden }
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
        if (query.isBlank()) return

        val searchPrefs = searchSettings.value
        if (!searchPrefs.autoOpenOnSearch) return

        val visibleApps = getVisibleAppsForSearch(searchPrefs)
        val favForBoosting = if (searchPrefs.favouriteBoostInSearch) {
            favouriteApps.value.map { it.packageName }.toSet()
        } else emptySet()

        val results = appSearchEngine.search(visibleApps, query, favForBoosting)
        if (results.size == 1) {
            onAppOpened(results.first())
        }
    }

    fun onSearchDone() {
        val query = searchQuery.value
        if (query.isBlank()) return

        val searchPrefs = searchSettings.value
        val visibleApps = getVisibleAppsForSearch(searchPrefs)
        val favForBoosting = if (searchPrefs.favouriteBoostInSearch) {
            favouriteApps.value.map { it.packageName }.toSet()
        } else emptySet()

        val results = appSearchEngine.search(visibleApps, query, favForBoosting)
        val firstApp = results.firstOrNull() ?: return

        onAppOpened(firstApp)
    }

    fun onAppOpened(app: AppInfo) {
        viewModelScope.launch {
            launchCoordinator.requestLaunch(app)
        }
        requestToGoHome()
    }

    fun onOpenAppInfo(app: AppInfo) {
        viewModelScope.launch {
            val result = intentLauncher.openAppInfo(app)
            handleLaunchResult(result)
        }
        onBottomSheetDismissed()
    }

    fun onUninstallApp(app: AppInfo) {
        viewModelScope.launch {
            val result = intentLauncher.uninstallApp(app)
            handleLaunchResult(result)
        }
        onBottomSheetDismissed()
    }

    fun onAppLongPressed(app: AppInfo, profile: AppProfile = AppProfile.Standard) {
        val isFavourite = favouriteApps.value.any { it.packageName == app.packageName }
        val isCountdownRequired = countdownPackages.value.any { it == app.packageName }

        // Edit the blank sheet state asap.
        _bottomSheetState.value = BottomSheetState.AppOptions(
            SelectedApp(
                app,
                isFavourite,
                isCountdownRequired,
                profile
            ),
            shortcuts = emptyList()
        )

        viewModelScope.launch {
            val shortcuts = shortcutRepository.getShortcuts(app)
            val currentState = _bottomSheetState.value

            if (currentState is BottomSheetState.AppOptions && currentState.selectedApp.app.packageName == app.packageName) {
                _bottomSheetState.value = currentState.copy(shortcuts = shortcuts)
            }
        }
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

    fun onToggleFavourite(packageName: String) {
        viewModelScope.launch {
            if (favouritePackages.value.contains(packageName)) {
                favouriteAppsRepository.removeFavouriteApp(packageName)
            } else {
                favouriteAppsRepository.addFavouriteApp(packageName)
            }
        }
        onBottomSheetDismissed()
    }

    fun onToggleCountdown(packageName: String) {
        viewModelScope.launch {
            if (countdownPackages.value.contains(packageName)) {
                countdownRepository.removeCountdownApp(packageName)
            } else {
                countdownRepository.addCountdownApp(packageName)
            }
        }
        onBottomSheetDismissed()
    }

    fun onHideApp(packageName: String) {
        viewModelScope.launch {
            hiddenAppsRepository.addHiddenApp(packageName)
        }
        onBottomSheetDismissed()
    }

    fun onLaunchShortcut(shortcut: AppShortcut) {
        viewModelScope.launch {
            val result = intentLauncher.launchShortcut(shortcut)
            handleLaunchResult(result)
        }
        onBottomSheetDismissed()
    }
}
