package com.lumina.domain.coordination

interface DeviceUserProvider {
    fun getAllUsersSerialNumber(): List<Long>
    fun getCurrentUserSerialNumber(): Long
    fun getProfileName(serialNumber: Long): String
}
