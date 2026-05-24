package com.lumina.domain.coordination

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class TriggerSystemStateCache @Inject constructor() {
    private val _currentSsid = MutableStateFlow<String?>(null)
    val currentSsid: StateFlow<String?> = _currentSsid.asStateFlow()

    private val _connectedDevices = MutableStateFlow<Set<String>>(emptySet())
    val connectedDevices: StateFlow<Set<String>> = _connectedDevices.asStateFlow()

    private val _activeGeofencesId = MutableStateFlow<Set<String>>(emptySet())
    val activeGeofenceIds: StateFlow<Set<String>> = _activeGeofencesId.asStateFlow()

    fun updateSsid(ssid: String?) {
        _currentSsid.value = ssid
    }

    fun addConnectedDevice(address: String) {
        _connectedDevices.value += address
    }

    fun removeConnectedDevice(address: String) {
        _connectedDevices.value -= address
    }

    fun clearConnectedDevices() {
        _connectedDevices.value = emptySet()
    }

    fun addActiveGeofence(requestId: String) {
        _activeGeofencesId.value += requestId
    }

    fun removeActiveGeofence(requestId: String) {
        _activeGeofencesId.value -= requestId
    }
}
