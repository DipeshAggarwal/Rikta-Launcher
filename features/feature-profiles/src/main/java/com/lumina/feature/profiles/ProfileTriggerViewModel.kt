package com.lumina.feature.profiles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.android.datasource.BluetoothDataSource
import com.lumina.core.android.datasource.BluetoothDeviceInfo
import com.lumina.core.android.datasource.LocationDataSource
import com.lumina.core.android.datasource.LocationInfo
import com.lumina.core.android.datasource.WifiDataSource
import com.lumina.core.android.datasource.WifiNetworkInfo
import com.lumina.core.common.IoDispatcher
import com.lumina.core.logging.Logger
import com.lumina.core.model.LogicalOperator
import com.lumina.core.model.ProfileTriggerType
import com.lumina.domain.coordination.TriggerScheduler
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.TriggerCondition
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.util.Calendar

sealed interface ProfileTriggerEvent {
    data object SaveSuccess : ProfileTriggerEvent
    data object DeleteSuccess : ProfileTriggerEvent
}

data class ProfileTriggerUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isNewTrigger: Boolean = false,

    val profileId: String = "",
    val triggerId: Long = 0L,
    val sequenceOrder: Int = 0,

    val triggerType: ProfileTriggerType = ProfileTriggerType.TIME,
    val logicalOperator: LogicalOperator? = null,
    val stopIfTrue: Boolean = false,

    val startTimeMinutes: Int? = null,
    val endTimeMinutes: Int? = null,

    val selectedDays: Set<DayOfWeek> = emptySet(),

    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusMeters: Float? = null,

    val wifiSsid: String? = null,
    val bluetoothAddress: String? = null,

    val errorMessage: String? = null
)

private const val DEFAULT_RADIUS = 250f

@HiltViewModel
class ProfileTriggerViewModel @Inject constructor(
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val profileRepository: ProfileRepository,
    private val triggerScheduler: TriggerScheduler,
    private val wifiDataSource: WifiDataSource,
    private val bluetoothDataSource: BluetoothDataSource,
    private val locationDataSource: LocationDataSource,
    savedStateHandle: SavedStateHandle,
    private val logger: Logger
) : ViewModel() {
    private val TAG = this::class.java.simpleName

    // Profile being viewed, provided by navigation.
    private val targetProfileId: String = checkNotNull(savedStateHandle[ProfileNavigationRoute.PROFILE_ID_ARG])
    private val targetTriggerId: Long = savedStateHandle.get<String>(ProfileNavigationRoute.TRIGGER_ID_ARG)
        ?.toLongOrNull()
        ?: 0L

    private val _uiState = MutableStateFlow(
        ProfileTriggerUiState(
            isLoading =  targetTriggerId != 0L,
            isNewTrigger = targetTriggerId == 0L,
            profileId = targetProfileId,
            triggerId = targetTriggerId,
            triggerType = if (targetTriggerId == 0L) {
                checkNotNull(
                    savedStateHandle.get<String>(ProfileNavigationRoute.TRIGGER_TYPE_ARG)
                        ?.let { runCatching { ProfileTriggerType.valueOf(it) }.getOrNull() }
                ) { "Trigger Type is required for new triggers." }
            } else {
                ProfileTriggerType.TIME
            }
        )
    )
    val uiState: StateFlow<ProfileTriggerUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileTriggerEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<ProfileTriggerEvent> = _events.asSharedFlow()

    private val _availableNetworks = MutableStateFlow<List<WifiNetworkInfo>>(emptyList())
    val availableNetworks: StateFlow<List<WifiNetworkInfo>> = _availableNetworks.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDeviceInfo>> = _pairedDevices.asStateFlow()

    private val _currentLocationInfo = MutableStateFlow<LocationInfo?>(null)
    val currentLocationInfo: StateFlow<LocationInfo?> = _currentLocationInfo.asStateFlow()

    init {
        if (targetTriggerId != 0L) loadExistingTriggers()
        else loadSystemDataForType(_uiState.value.triggerType)
    }

    private fun loadExistingTriggers() {
        viewModelScope.launch {
            val trigger = profileRepository.getTriggerById(targetTriggerId)

            if (trigger != null) {
                val type = trigger.triggerType ?: ProfileTriggerType.TIME
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        sequenceOrder = trigger.sequenceOrder,
                        triggerType = trigger.triggerType,
                        logicalOperator = trigger.logicalOperator,
                        stopIfTrue = trigger.stopIfTrue,
                        startTimeMinutes = trigger.startTimeMinutes,
                        endTimeMinutes = trigger.endTimeMinutes,
                        selectedDays = parseDaysOfWeek(trigger.daysOfWeek),
                        latitude = trigger.latitude,
                        longitude = trigger.longitude,
                        radiusMeters = trigger.radiusMeters,
                        wifiSsid = trigger.wifiSsid,
                        bluetoothAddress = trigger.bluetoothAddress
                    )
                }
                loadSystemDataForType(type)
            } else {
                logger.w(TAG, "Trigger not found: $targetTriggerId.")
                _uiState.update { it.copy(isLoading = false, errorMessage = "Trigger not found.") }
            }
        }
    }

    private fun loadSystemDataForType(type: ProfileTriggerType) {
        when (type) {
            ProfileTriggerType.TIME -> Unit
            ProfileTriggerType.DAY -> Unit
            ProfileTriggerType.LOCATION -> viewModelScope.launch(ioDispatcher) {
                _currentLocationInfo.value = locationDataSource.getLastKnownLocation()
            }
            ProfileTriggerType.WIFI -> viewModelScope.launch(ioDispatcher) {
                _availableNetworks.value = wifiDataSource.getAvailableNetworks()
            }
            ProfileTriggerType.BLUETOOTH -> viewModelScope.launch(ioDispatcher) {
                _pairedDevices.value = bluetoothDataSource.getPairedDevices()
            }
        }
    }

    private fun parseDaysOfWeek(days: List<Int>?): Set<DayOfWeek> {
        if (days.isNullOrEmpty()) return emptySet()
        return days.mapNotNull { it.toDayOfWeekOrNull() }.toSet()
    }

    private fun validate(state: ProfileTriggerUiState): String? {
        return when (state.triggerType) {
            ProfileTriggerType.TIME -> when {
                state.startTimeMinutes == null || state.endTimeMinutes == null ->
                    "Start and end times are required."
                state.selectedDays.isEmpty() ->
                    "Select at least one day."
                else -> null
            }

            ProfileTriggerType.DAY -> {
                if (state.selectedDays.isEmpty()) "Select at least one day." else null
            }

            ProfileTriggerType.LOCATION -> when {
                state.latitude == null || state.longitude == null -> "Select a location."
                state.radiusMeters == null -> "Select a radius."
                state.radiusMeters <= 0f -> "Radius must be greater than zero."
                else -> null
            }

            ProfileTriggerType.WIFI -> {
                if (state.wifiSsid.isNullOrBlank()) "Enter a Wi-Fi network name." else null
            }

            ProfileTriggerType.BLUETOOTH -> {
                if (state.bluetoothAddress.isNullOrBlank()) "Select a Bluetooth device." else null
            }
        }
    }

    fun refreshLocation() {
        viewModelScope.launch(ioDispatcher) {
            _currentLocationInfo.value = locationDataSource.getLastKnownLocation()
        }
    }

    fun useCurrentLocation() {
        val info = _currentLocationInfo.value ?: return
        updateLocation(info.latitude, info.longitude, _uiState.value.radiusMeters ?: DEFAULT_RADIUS)
    }

    fun updateTriggerType(type: ProfileTriggerType) {
        _uiState.update { it.copy(triggerType = type, errorMessage = null) }
    }

    fun updateLogicalOperator(operator: LogicalOperator?) {
        _uiState.update { it.copy(logicalOperator = operator) }
    }

    fun updateStopIfTrue(stop: Boolean) {
        _uiState.update { it.copy(stopIfTrue = stop) }
    }

    fun updateTimeRange(start: Int, end: Int) {
        _uiState.update { it.copy(startTimeMinutes = start, endTimeMinutes = end, errorMessage = null) }
    }

    fun toggleDayOfWeek(day: DayOfWeek) {
        _uiState.update { state ->
            val updated = if (day in state.selectedDays) state.selectedDays - day
                else state.selectedDays + day
            state.copy(selectedDays = updated, errorMessage = null)
        }
    }

    fun updateLocation(lat: Double, lng: Double, radius: Float) {
        _uiState.update { it.copy(latitude = lat, longitude = lng, radiusMeters = radius, errorMessage = null) }
    }

    fun updateRadius(radius: Float) {
        _uiState.update { it.copy(radiusMeters = radius.coerceAtLeast(1f), errorMessage = null) }
    }

    fun updateWifiSsid(ssid: String) {
        _uiState.update { it.copy(wifiSsid = ssid, errorMessage = null) }
    }

    fun updateBluetoothAddress(address: String) {
        _uiState.update { it.copy(bluetoothAddress = address, errorMessage = null) }
    }

    fun saveTrigger() {
        viewModelScope.launch {
            if (_uiState.value.isSaving) return@launch

            val state = _uiState.value
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val validationError = validate(state)
            if (validationError != null) {
                _uiState.update { it.copy(isSaving = false, errorMessage = validationError) }
                return@launch
            }

            val condition = TriggerCondition.create(
                triggerId = state.triggerId,
                profileId = state.profileId,
                sequenceOrder = state.sequenceOrder,
                triggerType = state.triggerType,
                logicalOperator = state.logicalOperator,
                stopIfTrue = state.stopIfTrue,
                startTimeMinutes = state.startTimeMinutes,
                endTimeMinutes = state.endTimeMinutes,
                daysOfWeek = state.selectedDays.map { it.toCalendarInt() },
                latitude = state.latitude,
                longitude = state.longitude,
                radiusMeters = state.radiusMeters,
                wifiSsid = state.wifiSsid,
                bluetoothAddress = state.bluetoothAddress
            )

            try {
                if (state.isNewTrigger) profileRepository.addProfileTrigger(state.profileId, condition)
                else profileRepository.updateProfileTrigger(condition)
                triggerScheduler.refresh()
                _events.emit(ProfileTriggerEvent.SaveSuccess)
            } catch (e: Exception) {
                logger.e(TAG, "Failed to save trigger for profile: $targetProfileId.", e)
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to save trigger.") }
            }
        }
    }

    fun deleteTrigger() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isNewTrigger || state.isSaving) return@launch

            _uiState.update { it.copy(isSaving = true) }
            try {
                profileRepository.removeProfileTrigger(state.triggerId)
                triggerScheduler.refresh()
                _events.emit(ProfileTriggerEvent.DeleteSuccess)

                // Do not reset state here, otherwise it flashes with the deleted profile data.
            } catch (e: Exception) {
                logger.e(TAG, "Failed to delete trigger: $targetTriggerId.", e)
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to delete trigger.") }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun Int.toDayOfWeekOrNull(): DayOfWeek? = when (this) {
        Calendar.SUNDAY -> DayOfWeek.SUNDAY
        Calendar.MONDAY -> DayOfWeek.MONDAY
        Calendar.TUESDAY -> DayOfWeek.TUESDAY
        Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
        Calendar.THURSDAY -> DayOfWeek.THURSDAY
        Calendar.FRIDAY -> DayOfWeek.FRIDAY
        Calendar.SATURDAY -> DayOfWeek.SATURDAY
        else -> null
    }

    private fun DayOfWeek.toCalendarInt(): Int = when (this) {
        DayOfWeek.SUNDAY -> Calendar.SUNDAY
        DayOfWeek.MONDAY -> Calendar.MONDAY
        DayOfWeek.TUESDAY -> Calendar.TUESDAY
        DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY
        DayOfWeek.THURSDAY -> Calendar.THURSDAY
        DayOfWeek.FRIDAY -> Calendar.FRIDAY
        DayOfWeek.SATURDAY -> Calendar.SATURDAY
    }
}
