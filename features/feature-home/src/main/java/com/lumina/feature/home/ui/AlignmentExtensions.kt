package com.lumina.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import com.lumina.domain.settings.AppAlignment
import com.lumina.domain.settings.HomeAlignment
import com.lumina.domain.settings.HomeVerticalAlignment

fun AppAlignment.toAlignment(): Alignment.Horizontal = when(this) {
    AppAlignment.Start -> Alignment.Start
    AppAlignment.Center -> Alignment.CenterHorizontally
    AppAlignment.End -> Alignment.End
}

fun HomeAlignment.toAlignment(): Alignment.Horizontal = when(this) {
    HomeAlignment.Start -> Alignment.Start
    HomeAlignment.Center -> Alignment.CenterHorizontally
    HomeAlignment.End -> Alignment.End
}

fun HomeVerticalAlignment.toAlignment(): Arrangement.Vertical = when(this) {
    HomeVerticalAlignment.Top -> Arrangement.Top
    HomeVerticalAlignment.Center -> Arrangement.Center
    HomeVerticalAlignment.Bottom -> Arrangement.Bottom
}
