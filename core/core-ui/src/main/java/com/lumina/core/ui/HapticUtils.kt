package com.lumina.core.ui

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object HapticUtils {
    /**
     * Performs haptic feedback with long press intensity.
     */
    fun performHapticFeedback(hapticFeedback: HapticFeedback) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}
