package com.lumina.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lumina.core.common.AppTheme
import com.lumina.core.ui.layout.LayoutSpacing
import com.lumina.core.ui.layout.LocalLayoutSpacing
import com.materialkolor.DynamicMaterialTheme

@Composable
fun RiktaTheme(
    theme: AppTheme,
    fontFamily: FontFamily,
    spacerHeight: Dp,
    content: @Composable (() -> Unit)
) {
    val themeSeed = theme.resolveColorSeed()
    val isAmoled = themeSeed.isAmoled

    var themeSeedColor = themeSeed.seedColor
    var isDark = themeSeed.isDark

    if (themeSeed.isSystem) {
        isDark = isSystemInDarkTheme()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            themeSeedColor = if(isDark) {
                dynamicDarkColorScheme(LocalContext.current).primary
            } else {
                dynamicLightColorScheme(LocalContext.current).primary
            }
        } else {
            themeSeedColor = if (isSystemInDarkTheme()){
                AppTheme.LIGHT.resolveColorSeed().seedColor
            } else {
                AppTheme.PURE_BLACK.resolveColorSeed().seedColor
            }
        }

    }

    // Allow every module to access Spacer Height.
    CompositionLocalProvider(
        LocalLayoutSpacing provides LayoutSpacing(spacerHeight)
    ) {
        DynamicMaterialTheme(
            seedColor = themeSeedColor,
            isDark = isDark,
            isAmoled = isAmoled,
            typography = RiktaTypography(fontFamily),
            animate = true,
            content = content
        )
    }
}
