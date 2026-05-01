package com.lumina.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.lumina.core.common.AppTheme
import com.materialkolor.palettes.CorePalette

private fun Int.toComposeColor(): Color = Color(this)
/**
 * Theme seed configuration for MaterialKolor dynamic color generation.
 *
 * Contains the seed color and dark mode settings used to generate
 * a full Material 3 color scheme via MaterialKolor's CorePalette.
 *
 * Also provides direct access to specific colors for ThemeCard previews
 * where we need to show theme colors before MaterialTheme is applied.
 */
data class ThemeSeed(
    val seedColor: Color,
    val isDark: Boolean = false,
    val isAmoled: Boolean = false,
    val isSystem: Boolean = false
) {
    val palette = CorePalette.of(seedColor.toArgb())
    val background get() = computeBackground()
    val primary get() = computePrimary()
    val onPrimary get() = computeOnPrimary()
    val onPrimaryContainer get() = computeOnPrimaryContainer()

    fun computeBackground(): Color {
        return if (isAmoled) {
            palette.n1.tone(TonalToken.TONE_AMOLED_BG).toComposeColor()
        } else if (isDark) {
            palette.n1.tone(TonalToken.TONE_DARK_BG).toComposeColor()
        } else {
            palette.n1.tone(TonalToken.TONE_LIGHT_BG).toComposeColor()
        }
    }

    fun computePrimary(): Color {
        return if (isDark) {
            palette.a1.tone(TonalToken.TONE_DARK_PRIMARY).toComposeColor()
        } else {
            palette.a1.tone(TonalToken.TONE_LIGHT_PRIMARY).toComposeColor()
        }
    }

    fun computeOnPrimary(): Color {
        return if (isDark) {
            palette.a1.tone(TonalToken.TONE_DARK_ON_PRIMARY).toComposeColor()
        } else {
            palette.a1.tone(TonalToken.TONE_LIGHT_ON_PRIMARY).toComposeColor()
        }
    }

    fun computeOnPrimaryContainer(): Color {
        return if (isDark) {
            palette.a1.tone(TonalToken.TONE_DARK_ON_PRIMARY_CONT).toComposeColor()
        } else {
            palette.a1.tone(TonalToken.TONE_LIGHT_ON_PRIMARY_CONT).toComposeColor()
        }
    }
}

@Composable
fun AppTheme.resolveColorSeed(): ThemeSeed {
    return when (this) {
        AppTheme.PURE_BLACK -> ThemeSeed(
            seedColor = ColorSeedTokens.PureBlack,
            isDark = true,
            isAmoled = true
        )

        AppTheme.LIGHT -> ThemeSeed(
            seedColor = ColorSeedTokens.Light,
            isDark = false
        )
        AppTheme.WARM_LIGHT -> ThemeSeed(
            seedColor = ColorSeedTokens.WarmLight,
            isDark = true
        )
        AppTheme.COOL_DARK -> ThemeSeed(
            seedColor = ColorSeedTokens.CoolDark,
            isDark = true
        )

        AppTheme.LIGHT_RED -> ThemeSeed(
            seedColor = ColorSeedTokens.Red,
            isDark = false
        )
        AppTheme.DARK_RED -> ThemeSeed(
            seedColor = ColorSeedTokens.Red,
            isDark = true
        )

        AppTheme.LIGHT_GREEN -> ThemeSeed(
            seedColor = ColorSeedTokens.Green,
            isDark = false
        )
        AppTheme.DARK_GREEN -> ThemeSeed(
            seedColor = ColorSeedTokens.Green,
            isDark = true
        )

        AppTheme.LIGHT_BLUE -> ThemeSeed(
            seedColor = ColorSeedTokens.Blue,
            isDark = false
        )
        AppTheme.DARK_BLUE -> ThemeSeed(
            seedColor = ColorSeedTokens.Blue,
            isDark = true
        )

        AppTheme.LIGHT_YELLOW -> ThemeSeed(
            seedColor = ColorSeedTokens.Yellow,
            isDark = false
        )
        AppTheme.DARK_YELLOW -> ThemeSeed(
            seedColor = ColorSeedTokens.Yellow,
            isDark = true
        )

        AppTheme.LIGHT_NEUTRAL -> ThemeSeed(
            seedColor = ColorSeedTokens.Neutral,
            isDark = false
        )

        AppTheme.DARK_NEUTRAL -> ThemeSeed(
            seedColor = ColorSeedTokens.Neutral,
            isDark = true
        )

        AppTheme.SYSTEM -> ThemeSeed(
            seedColor = ColorSeedTokens.Blank,
            isSystem = true
        )
    }
}
