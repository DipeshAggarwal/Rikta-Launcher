package com.lumina.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.common.FlowDefaults.WhileSubscribedTimeoutMs
import com.lumina.core.logging.Logger
import com.lumina.core.model.AppCategory
import com.lumina.core.model.AppInfo
import com.lumina.core.model.AppShortcut
import com.lumina.core.model.FavouriteItemType
import com.lumina.core.model.LauncherItem
import com.lumina.core.model.componentKey
import com.lumina.domain.apps.AppOverrideRepository
import com.lumina.domain.apps.AppShortcutRepository
import com.lumina.domain.apps.HiddenAppsRepository
import com.lumina.domain.search.AppSearchEngine
import com.lumina.domain.settings.AppListSettings
import com.lumina.domain.settings.HomeSettings
import com.lumina.domain.settings.SearchSettings
import com.lumina.domain.coordination.AppLaunchCoordinator
import com.lumina.domain.coordination.IntentLauncher
import com.lumina.domain.coordination.LaunchResult
import com.lumina.domain.coordination.StatusBarController
import com.lumina.domain.coordination.model.ResolvedThemeState
import com.lumina.domain.coordination.model.ResolvedUIState
import com.lumina.domain.coordination.usecase.GetActiveHomeConfigUseCase
import com.lumina.domain.coordination.usecase.GetAssignedProfilesForAppUseCase
import com.lumina.domain.coordination.usecase.ObserveActiveProfileAppsMappingUseCase
import com.lumina.domain.coordination.usecase.ObserveActiveProfileAppsUseCase
import com.lumina.domain.coordination.usecase.ObserveActiveProfileFavouritesUseCase
import com.lumina.domain.coordination.usecase.ObserveAllProfileUseCase
import com.lumina.domain.profiles.ProfileFavouriteRepository
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.settings.CountdownSettings
import com.lumina.domain.settings.LayoutSettings
import com.lumina.domain.shortcut.ShortcutRepository
import com.lumina.feature.home.model.BottomSheetState
import com.lumina.feature.home.model.HomeUiState
import com.lumina.feature.home.model.SelectedApp
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val hiddenAppsRepository: HiddenAppsRepository,
    private val profileRepository: ProfileRepository,
    private val profileFavouriteRepository: ProfileFavouriteRepository,
    private val appOverrideRepository: AppOverrideRepository,
    private val shortcutRepository: ShortcutRepository,
    observeAppMapping: ObserveActiveProfileAppsMappingUseCase,
    observeActiveProfileApps: ObserveActiveProfileAppsUseCase,
    observeActiveProfileFavourites: ObserveActiveProfileFavouritesUseCase,
    observeAllProfiles: ObserveAllProfileUseCase,
    getActiveHomeConfig: GetActiveHomeConfigUseCase,
    private val getAssignedProfilesForApp: GetAssignedProfilesForAppUseCase,
    private val appSearchEngine: AppSearchEngine,
    private val intentLauncher: IntentLauncher,
    private val launchCoordinator: AppLaunchCoordinator,
    private val appShortcutRepository: AppShortcutRepository,
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
    private val activeProfileApps: StateFlow<List<LauncherItem.App>> = observeActiveProfileApps()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val activeProfileFavourites: StateFlow<List<LauncherItem>> = observeActiveProfileFavourites()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val rawAppsMap: StateFlow<Map<String, LauncherItem.App>> = observeAppMapping()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val availableProfiles: StateFlow<List<Pair<String, String>>> = observeAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMs), emptyList())

    val resolvedUiState: StateFlow<ResolvedUIState> = getActiveHomeConfig()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ResolvedUIState(
                HomeSettings(), AppListSettings(),
                SearchSettings(), CountdownSettings(),
                LayoutSettings(), ResolvedThemeState(null, null)
            )
        )

    // For future ref: Combine can only take a maximum of five Flows.
    // This is recomputed anytime any of the source changes.
    val homeUiState: StateFlow<HomeUiState> = combine(
        activeProfileApps,
        activeProfileFavourites,
        searchQuery,
        resolvedUiState,
        rawAppsMap
    ) { apps, favItems, query, resolvedState, rawApps ->
        val searchPrefs = resolvedState.search
        val isSearching = query.isNotBlank()

        val favForBoosting = if (searchPrefs.favouriteBoostInSearch) {
            favItems.mapNotNull {
                when (it) {
                    is LauncherItem.App -> it.info.packageName
                    is LauncherItem.Shortcut -> it.targetPackage
                }
            }.toSet()
        } else emptySet()

        val finalAppsList = if (isSearching) {
            val searchableApps = getVisibleAppsForSearch(searchPrefs, apps)
            val searchResults = appSearchEngine.search(searchableApps, query, favForBoosting)

            searchResults.mapNotNull { rawApps[it.packageName] }
        } else {
            apps
        }

        HomeUiState.Ready(
            favItems,
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

    private fun getVisibleAppsForSearch(
        searchPrefs: SearchSettings,
        activeProfileApps: List<LauncherItem.App>
    ): List<AppInfo> {
        return if (searchPrefs.showHiddenAppsInSearch) {
            rawAppsMap.value.values.map { it.info }.toList()
        } else {
            activeProfileApps.map { it.info }
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
        if (query.isBlank()) return

        val searchPrefs = resolvedUiState.value.search
        if (!searchPrefs.autoOpenOnSearch) return

        val visibleApps = getVisibleAppsForSearch(searchPrefs, activeProfileApps.value)
        val favForBoosting = if (searchPrefs.favouriteBoostInSearch) {
            activeProfileFavourites.value.mapNotNull {
                (it as? LauncherItem.App)?.info?.packageName
            }.toSet()
        } else emptySet()

        val results = appSearchEngine.search(visibleApps, query, favForBoosting)
        if (results.size == 1) {
            val matchedApp = results.first()
            val launcherApp = rawAppsMap.value[matchedApp.packageName]

            if (launcherApp != null) {
                onAppOpened(launcherApp)
            }
        }
    }

    fun onSearchDone() {
        val query = searchQuery.value
        if (query.isBlank()) return

        val searchPrefs = resolvedUiState.value.search
        val visibleApps = getVisibleAppsForSearch(searchPrefs, activeProfileApps.value)

        val favForBoosting = if (searchPrefs.favouriteBoostInSearch) {
            activeProfileFavourites.value.mapNotNull {
                (it as? LauncherItem.App)?.info?.packageName
            }.toSet()
        } else emptySet()

        val results = appSearchEngine.search(visibleApps, query, favForBoosting)
        results.firstOrNull()?.let { matchedAppInfo ->
            rawAppsMap.value[matchedAppInfo.packageName]?.let { onAppOpened(it) }
        }
    }

    fun observeApp(componentKey: String): Flow<LauncherItem.App?> {
        return rawAppsMap.map { it[componentKey] }
    }

    fun onAppOpened(app: LauncherItem.App) {
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

    fun onItemLongPressed(item: LauncherItem) {
        val isFavourite = activeProfileFavourites.value.any { fav ->
            when (item) {
                is LauncherItem.App -> fav is LauncherItem.App && fav.info.packageName == item.info.packageName
                is LauncherItem.Shortcut -> fav is LauncherItem.Shortcut && fav.id == item.id
            }
        }

        when (item) {
            is LauncherItem.App -> {
                val app = item.info
                _bottomSheetState.value = BottomSheetState.AppOptions(
                    SelectedApp(
                        app = item,
                        isFavourite = isFavourite,
                        isCountdownRequired = item.showCountdown,
                        profileIds = emptySet()
                    ),
                    shortcuts = emptyList()
                )

                viewModelScope.launch {
                    val assignedProfileIds = getAssignedProfilesForApp(
                        item.info.packageName,
                        item.info.userHandleNumber
                    ).first()
                    val appShortcuts = appShortcutRepository.getShortcuts(app)
                    val currentState = _bottomSheetState.value

                    if (currentState is BottomSheetState.AppOptions &&
                        currentState.selectedApp.app.info.packageName == app.packageName
                    ) {
                        _bottomSheetState.value = currentState.copy(
                            selectedApp = currentState.selectedApp.copy(profileIds = assignedProfileIds),
                            shortcuts = appShortcuts
                        )
                    }
                }
            }

            is LauncherItem.Shortcut -> {
                _bottomSheetState.value = BottomSheetState.ShortcutOptions(
                    shortcut = item,
                    isFavourite = isFavourite
                )
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

    fun onToggleFavourite(item: LauncherItem, dismissSheet: Boolean = true) {
        viewModelScope.launch {
            val activeProfile = profileRepository.activeProfile.firstOrNull() ?: return@launch
            val (itemId, itemType) = when (item) {
                is LauncherItem.App -> item.info.componentKey to FavouriteItemType.APP
                is LauncherItem.Shortcut -> item.id to FavouriteItemType.SHORTCUT
            }

            profileFavouriteRepository.toggle(
                profileId = activeProfile.id,
                itemId = itemId,
                itemType = itemType
            )

            val currentState = _bottomSheetState.value
            if (currentState is BottomSheetState.AppOptions) {
                val currentApp = currentState.selectedApp
                if (currentApp.app.info.componentKey == itemId) {
                    _bottomSheetState.value = currentState.copy(
                        selectedApp = currentApp.copy(isFavourite = !currentApp.isFavourite)
                    )
                }
            }
        }
        if (dismissSheet) onBottomSheetDismissed()
    }

    fun onToggleCountdown(item: LauncherItem.App) {
        viewModelScope.launch {
            val activeProfile = profileRepository.activeProfile.firstOrNull() ?: return@launch
            profileRepository.updateShowCountdownForApp(
                profileId = activeProfile.id,
                packageName = item.info.packageName,
                userHandleNumber = item.info.userHandleNumber,
                show = !item.showCountdown
            )
        }
        onBottomSheetDismissed()
    }

    fun onHideApp(packageName: String) {
        viewModelScope.launch {
            hiddenAppsRepository.addApp(packageName)
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

    fun onLaunchProfileShortcut(shortcut: LauncherItem.Shortcut) {
        viewModelScope.launch {
            val result = intentLauncher.launchProfileShortcut(shortcut)
            handleLaunchResult(result)
        }
        onBottomSheetDismissed()
    }

    fun onDeleteShortcut(shortcut: LauncherItem.Shortcut) {
        viewModelScope.launch {
            shortcutRepository.delete(shortcut.id)
        }
    }

    fun onRenameApp(app: AppInfo, newDisplayName: String, dismissSheet: Boolean = false) {
        viewModelScope.launch {
            val sanitisedName = newDisplayName.trim()
            if (sanitisedName.isNotBlank() && sanitisedName != app.displayName) {
                appOverrideRepository.setDisplayName(
                    packageName = app.packageName,
                    userHandleNumber = app.userHandleNumber,
                    displayName = sanitisedName
                )
            }
        }
        if (dismissSheet) onBottomSheetDismissed()
    }

    fun onResetNameApp(app: AppInfo, dismissSheet: Boolean = false) {
        viewModelScope.launch {
            appOverrideRepository.setDisplayName(
                packageName = app.packageName,
                userHandleNumber = app.userHandleNumber,
                displayName = null
            )
        }
    }

    fun onChangeCategory(app: AppInfo, newCategory: AppCategory?, dismissSheet: Boolean = false) {
        viewModelScope.launch {
            if (newCategory != app.category) {
                appOverrideRepository.setCategory(
                    packageName = app.packageName,
                    userHandleNumber = app.userHandleNumber,
                    category = newCategory,
                    customCategoryName = null
                )
            }
        }
        if (dismissSheet) onBottomSheetDismissed()
    }

    fun onResetCategory(app: AppInfo, dismissSheet: Boolean = false) {
        viewModelScope.launch {
            appOverrideRepository.setCategory(
                packageName = app.packageName,
                userHandleNumber = app.userHandleNumber,
                category = null,
                customCategoryName = null
            )
        }
    }

    fun onUpdateAppProfile(app: AppInfo, selectedProfileIds: Set<String>, dismissSheet: Boolean = false) {
        viewModelScope.launch {
            val allProfiles = availableProfiles.first()

            allProfiles.forEach { (profileId, _) ->
                if (selectedProfileIds.contains(profileId)) {
                    profileRepository.addAppToProfile(
                        profileId = profileId,
                        packageName = app.packageName,
                        userHandleNumber = app.userHandleNumber
                    )
                } else {
                    profileRepository.removeAppFromProfile(
                        profileId = profileId,
                        packageName = app.packageName,
                        userHandleNumber = app.userHandleNumber
                    )
                }
            }
        }
        if (dismissSheet) onBottomSheetDismissed()
    }
}
