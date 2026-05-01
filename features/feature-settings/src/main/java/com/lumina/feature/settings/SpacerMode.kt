package com.lumina.feature.settings

import androidx.annotation.StringRes

enum class SpacerMode(
    @param:StringRes val labelRes: Int,
    val value: Int,
) {
    COMPACT(R.string.set_spacer_size_compact, 20),
    STANDARD(R.string.set_spacer_size_standard, 30),
    RELAXED(R.string.set_spacer_size_relaxed, 40)
}
