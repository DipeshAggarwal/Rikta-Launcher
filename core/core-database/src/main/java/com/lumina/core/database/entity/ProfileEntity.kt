package com.lumina.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lumina.domain.profiles.model.ProfileType

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
    val strictMode: Boolean,

    val priorityTriggerLaunch: Boolean = false,
    val filterNotification: Boolean = false,
    val startDnd: Boolean = false,
    val showAppList: Boolean = true,

    val overrideBackground: String? = null,
    val overrideFont: String? = null,

    val overrideShowClock: Boolean? = null,
    val overrideShowBigClock: Boolean? = null,
    val overrideShowDate: Boolean? = null,
    val overrideShowWeather: Boolean? = null,

    val activationKey: String? = null,
    val requiresBiometric: Boolean? = null,

    val version: Int = VERSION
)
