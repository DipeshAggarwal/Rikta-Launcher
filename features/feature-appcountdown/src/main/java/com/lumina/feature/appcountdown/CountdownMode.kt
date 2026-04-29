package com.lumina.feature.appcountdown

import androidx.annotation.StringRes

enum class CountdownMode(
    @param:StringRes val labelRes: Int,
    val value: Int
) {
    SHORT(R.string.app_countdown_time_short, 2),
    NORMAL(R.string.app_countdown_time_normal, 3),
    LONG(R.string.app_countdown_time_long, 4);

    companion object {
        fun fromValue(value: Int): CountdownMode? {
            return entries.find { it.value == value }
        }
    }
}
