package com.lumina.feature.system.triggers

import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TriggerSystemStateCacheTest {
    private lateinit var cache: TriggerSystemStateCache

    private val wifiSsid = "RiktaWifi"
    private val bluetoothAddressOne = "AA:BB:CC:DD:EE:FF"
    private val bluetoothAddressTwo = "A1:B2:C3:D4:E5:F6"
    private val geofenceIdOne = "profile_test::1"
    private val geofenceIdTwo = "profile_test_2::2"

    @Before
    fun setup() {
        cache = TriggerSystemStateCache()
    }

    @Test
    fun `initial ssid is null`() {
        assertNull(cache.currentSsid.value)
    }

    @Test
    fun `updateSsid sets value`() {
        cache.updateSsid(wifiSsid)
        assertEquals(wifiSsid, cache.currentSsid.value)
    }

    @Test
    fun `updateSsid with null clears value`() {
        cache.updateSsid(wifiSsid)
        cache.updateSsid(null)
        assertNull(cache.currentSsid.value)
    }

    @Test
    fun `initial connected devices is empty`() {
        assertTrue(cache.connectedDevices.value.isEmpty())
    }

    @Test
    fun `add one bluetooth address`() {
        cache.addConnectedDevice(bluetoothAddressOne)
        assertTrue(cache.connectedDevices.value.contains(bluetoothAddressOne))
    }

    @Test
    fun `add multiple bluetooth address`() {
        cache.addConnectedDevice(bluetoothAddressOne)
        cache.addConnectedDevice(bluetoothAddressTwo)
        assertEquals(2, cache.connectedDevices.value.size)
    }

    @Test
    fun `remove bluetooth address`() {
        cache.addConnectedDevice(bluetoothAddressOne)
        cache.removeConnectedDevice(bluetoothAddressOne)
        assertFalse(cache.connectedDevices.value.contains(bluetoothAddressOne))
    }

    @Test
    fun `remove only specified bluetooth address`() {
        cache.addConnectedDevice(bluetoothAddressOne)
        cache.addConnectedDevice(bluetoothAddressTwo)
        cache.removeConnectedDevice(bluetoothAddressOne)

        assertFalse(cache.connectedDevices.value.contains(bluetoothAddressOne))
        assertTrue(cache.connectedDevices.value.contains(bluetoothAddressTwo))
    }

    @Test
    fun `clear all bluetooth address`() {
        cache.addConnectedDevice(bluetoothAddressOne)
        cache.addConnectedDevice(bluetoothAddressTwo)
        cache.clearConnectedDevices()
        assertTrue(cache.connectedDevices.value.isEmpty())
    }

    @Test
    fun `check duplicate addresses are not stored`() {
        cache.addConnectedDevice(bluetoothAddressOne)
        cache.addConnectedDevice(bluetoothAddressOne)
        cache.addConnectedDevice(bluetoothAddressOne)
        assertEquals(1, cache.connectedDevices.value.size)
    }

    @Test
    fun `initial active geofences is empty`() {
        assertTrue(cache.activeGeofenceIds.value.isEmpty())
    }

    @Test
    fun `add single geofence id`() {
        cache.addActiveGeofence(geofenceIdOne)
        assertTrue(cache.activeGeofenceIds.value.contains(geofenceIdOne))
    }

    @Test
    fun `add multiple geofence id`() {
        cache.addActiveGeofence(geofenceIdOne)
        cache.addActiveGeofence(geofenceIdTwo)
        assertEquals(2, cache.activeGeofenceIds.value.size)
    }

    @Test
    fun `remove geofence id`() {
        cache.addActiveGeofence(geofenceIdOne)
        cache.removeActiveGeofence(geofenceIdOne)
        assertFalse(cache.activeGeofenceIds.value.contains(geofenceIdOne))
    }

    @Test
    fun `remove only specified geofence id`() {
        cache.addActiveGeofence(geofenceIdOne)
        cache.addActiveGeofence(geofenceIdTwo)
        cache.removeActiveGeofence(geofenceIdOne)

        assertFalse(cache.activeGeofenceIds.value.contains(geofenceIdOne))
        assertTrue(cache.activeGeofenceIds.value.contains(geofenceIdTwo))
    }

    @Test
    fun `check duplicate geofence id are not stored`() {
        cache.addActiveGeofence(geofenceIdOne)
        cache.addActiveGeofence(geofenceIdOne)
        cache.addActiveGeofence(geofenceIdOne)
        assertEquals(1, cache.activeGeofenceIds.value.size)
    }
}
