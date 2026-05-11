package com.lumina.core.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.lumina.core.model.AppCategory
import com.lumina.core.model.AppUsageEnforcementMode
import com.lumina.core.model.ProfileAuthMethod
import com.lumina.core.model.ProfileBackground
import com.lumina.core.model.ProfileType

private const val VERSION = 1

@Entity(
    tableName = "profiles",
    indices = [Index("auth_activationKey", unique = true)]
)
data class ProfileEntity(
    @PrimaryKey val id: String,
    val userHandleNumber: Long,

    val type: ProfileType,
    val name: String,
    val description: String?,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    val isAdmin: Boolean,
    val priorityTriggerLaunch: Boolean = false,

    @Embedded(prefix = "settings_")
    val settings: ProfileSettings,

    @Embedded(prefix = "permissions_")
    val permissions: ProfilePermissions,

    @Embedded(prefix = "restrictions_")
    val restrictions: ProfileRestrictions,

    @Embedded(prefix = "auth_")
    val auth: ProfileAuth,

    @Embedded(prefix = "override_")
    val overrides: ProfileOverrides,

    val version: Int = VERSION
)

data class ProfileSettings(
    val startDnd: Boolean = false,
    val hideScreenTimeOnApps: Boolean = false,

    val maxAppCount: Int = 0,
    val autoAddCategoryApps: List<AppCategory> = emptyList()
)

data class ProfilePermissions(
    val allowLauncherSettingsChange: Boolean = false,
    val allowManagingApps: Boolean = false,
    val allowLauncherAppActions: Boolean = false,
    val allowProfileManagement: Boolean = false
)

data class ProfileRestrictions(
    val appUsageEnforcementMode: AppUsageEnforcementMode = AppUsageEnforcementMode.COUNTDOWN,

    val blockAppList: Boolean = false,
    val blockProfileTriggerSwitching: Boolean  = false,
    val blockUnauthorisedApps: Boolean = false,
    val blockRecentApps: Boolean = false,
    val blockSystemAppAdd: Boolean = false,
    val blockNotificationShade: Boolean = false,

    val filterNotifications: Boolean = false,
    val switchOnDeviceLock: Boolean = false,
)

data class ProfileAuth(
    val entryAuthMethod: ProfileAuthMethod = ProfileAuthMethod.NONE,
    val exitAuthMethod: ProfileAuthMethod = ProfileAuthMethod.NONE,
    val activationKey: String? = null
)

data class ProfileOverrides(
    val theme: String? = null,
    val background: ProfileBackground? = null,
    val font: String? = null,
    val showClock: Boolean? = null,
    val showBigClock: Boolean? = null,
    val showDate: Boolean? = null,
    val showWeather: Boolean? = null,
    val hideScreenTime: Boolean? = null,
    val iconName: String? = null
)

class ProfileConverters {
    @TypeConverter
    fun toUsageEnforcementMode(value: String): AppUsageEnforcementMode = enumValueOf(value)

    @TypeConverter
    fun fromUsageEnforcementMode(mode: AppUsageEnforcementMode): String = mode.name

    @TypeConverter
    fun toProfileType(value: String): ProfileType = enumValueOf(value)

    @TypeConverter
    fun fromProfileType(mode: ProfileType): String = mode.name

    @TypeConverter
    fun toProfileAuthMethod(value: String): ProfileAuthMethod = enumValueOf(value)

    @TypeConverter
    fun fromProfileBackground(mode: ProfileBackground): String = mode.name

    @TypeConverter
    fun toProfileBackground(value: String): ProfileBackground = enumValueOf(value)

    @TypeConverter
    fun fromProfileAuthMethod(mode: ProfileAuthMethod): String = mode.name

    @TypeConverter
    fun toAppCategoryList(value: String): List<AppCategory> {
        return if (value.isEmpty()) emptyList()
            else value.split(",").map { enumValueOf(it.trim()) }
    }

    @TypeConverter
    fun fromAppCategoryList(categories: List<AppCategory>): String = categories.joinToString(",") { it.name }
}
