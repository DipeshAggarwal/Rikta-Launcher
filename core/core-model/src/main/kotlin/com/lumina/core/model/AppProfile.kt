package com.lumina.core.model

sealed class AppProfile {
    data object Standard : AppProfile()
    data object Private : AppProfile()
    data object Work : AppProfile()

    data class Custom(val id: String, val label: String) : AppProfile()
}
