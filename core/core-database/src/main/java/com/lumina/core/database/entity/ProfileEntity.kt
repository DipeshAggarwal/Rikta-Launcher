package com.lumina.core.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lumina.core.model.ProfileAuthMethod
import com.lumina.core.model.ProfileType

private const val VERSION = 1

@Entity(
    tableName = "profiles",
    indices = [Index("activationKey", unique = true)]
)
data class ProfileEntity(
    @PrimaryKey val id: String,
    val userHandleNumber: Long,

    val type: ProfileType,
    val name: String,
    val description: String?,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    @Embedded
    val settings: ProfileSettings,

    @Embedded(prefix = "override_")
    val overrides: ProfileOverrides,

    val version: Int = VERSION
)

data class ProfileSettings(
    val strictMode: Boolean,
    val isAdmin: Boolean,

    val priorityTriggerLaunch: Boolean = false,
    val filterNotification: Boolean = false,

    val allowAppRename: Boolean = false,
    val allowProfileManagement: Boolean = false,
    val allowAppCategoryChange: Boolean = false,

    val blockProfileTriggerSwitching: Boolean  = false,

    val startDnd: Boolean = false,
    val showAppList: Boolean = true,
    val hideScreenTimeOnApps: Boolean = false,
    val disableOnLock: Boolean = false,

    val blockUnauthorisedApps: Boolean = false,

    val entryAuthMethod: ProfileAuthMethod = ProfileAuthMethod.NONE,
    val exitAuthMethod: ProfileAuthMethod = ProfileAuthMethod.NONE,
    val activationKey: String? = null
)

data class ProfileOverrides(
    val theme: String? = null,
    val background: String? = null,
    val font: String? = null,
    val showClock: Boolean? = null,
    val showBigClock: Boolean? = null,
    val showDate: Boolean? = null,
    val showWeather: Boolean? = null,
    val hideScreenTime: Boolean? = null,
    val iconName: String? = null
)
