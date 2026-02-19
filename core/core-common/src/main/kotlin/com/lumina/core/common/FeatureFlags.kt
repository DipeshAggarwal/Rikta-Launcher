package com.lumina.core.common

/**
 * This handles migration flags.
 *
 * Rules:
 *     - Every new change is behind a feature flag first.
 *     - They default to false, and are true only for testing.
 */

object FeatureFlags {
    const val USE_NEW_SPACER_CONFIG = true
}
