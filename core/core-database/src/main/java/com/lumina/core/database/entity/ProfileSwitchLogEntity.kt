package com.lumina.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "profile_switch_log",
    indices = [Index("switchedAt")]
)
data class ProfileSwitchLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: String,
    val switchedAt: Long
)
