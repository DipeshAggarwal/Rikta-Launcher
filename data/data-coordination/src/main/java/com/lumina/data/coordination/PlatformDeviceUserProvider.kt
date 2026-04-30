package com.lumina.data.coordination

import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserManager
import com.lumina.domain.coordination.DeviceUserProvider
import jakarta.inject.Inject

class PlatformDeviceUserProvider @Inject constructor(
    private val userManager: UserManager,
    private val launcherApps: LauncherApps
) : DeviceUserProvider {
    override fun getAllUsersSerialNumber(): List<Long> {
        return userManager.userProfiles.map { handle ->
            userManager.getSerialNumberForUser(handle)
        }
    }

    override fun getCurrentUserSerialNumber(): Long {
        val handle = Process.myUserHandle()
        return userManager.getSerialNumberForUser(handle)
    }

    override fun getProfileName(serialNumber: Long): String {
        val handle = userManager.getUserForSerialNumber(serialNumber) ?: return "Personal"
        if (handle == Process.myUserHandle()) return "Personal"

        val restrictions = userManager.getUserRestrictions(handle)
        return when {
            restrictions.getBoolean(UserManager.DISALLOW_MODIFY_ACCOUNTS) -> "Work"
            else -> "OEM Profile"
        }
    }
}

