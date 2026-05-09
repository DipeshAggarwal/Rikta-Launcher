package com.lumina.feature.profiles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.logging.Logger
import com.lumina.core.model.LogicalOperator
import com.lumina.core.model.ProfileTriggerType
import com.lumina.domain.coordination.TriggerScheduler
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.TriggerCondition
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
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

@HiltViewModel
class ProfileTriggerViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val triggerScheduler: TriggerScheduler,
    savedStateHandle: SavedStateHandle,
    private val logger: Logger
) : ViewModel() {
    private val TAG = this::class.java.simpleName

    // Profile being viewed, provided by navigation.
    private val targetProfileId: String = checkNotNull(savedStateHandle[ProfileNavigationRoute.PROFILE_ID_ARG])
    private val targetTriggerId: Long = savedStateHandle[ProfileNavigationRoute.TRIGGER_ID_ARG] ?: 0L

    private val _uiState = MutableStateFlow(
        ProfileTriggerUiState(
            isLoading =  targetTriggerId != 0L,
            isNewTrigger = targetTriggerId == 0L,
            profileId = targetProfileId,
            triggerId = targetTriggerId
        )
    )
    val uiState: StateFlow<ProfileTriggerUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileTriggerEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<ProfileTriggerEvent> = _events.asSharedFlow()

    init { if (targetTriggerId != 0L) loadExistingTriggers() }

    private fun loadExistingTriggers() {
        viewModelScope.launch {
            val trigger = profileRepository.getProfileTriggers(targetProfileId)
                .firstOrNull()
                ?.find { it.triggerId == targetTriggerId }

            if (trigger != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        sequenceOrder = trigger.sequenceOrder,
                        triggerType = trigger.triggerType ?: ProfileTriggerType.TIME,
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
            } else {
                logger.w(TAG, "Trigger not found: $targetTriggerId.")
                _uiState.update { it.copy(isLoading = false, errorMessage = "Trigger not found.") }
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

    fun updateWifiSsid(ssid: String) {
        _uiState.update { it.copy(wifiSsid = ssid, errorMessage = null) }
    }

    fun updateBluetoothAddress(address: String) {
        _uiState.update { it.copy(bluetoothAddress = address, errorMessage = null) }
    }

    fun saveTrigger() {
        viewModelScope.launch {
            if (_uiState.value.isSaving) return@launch
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val state = _uiState.value
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
}
