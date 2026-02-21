package com.lumina.feature.home.model

sealed interface HomePage {
    data object ScreenTime: HomePage
    data object Main: HomePage
    data object Apps: HomePage
}
