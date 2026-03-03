package com.lumina.core.ui.components.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumina.core.ui.R
import com.lumina.core.ui.layout.LocalLayoutSpacing
import com.lumina.core.ui.theme.ContentColor
import com.lumina.core.ui.theme.DividerColor

private object SettingsSubheadingDefaults {
    val BottomPadding = 24.dp
}

private object SettingsSubHeaderDefaults {
    val LetterSpacing = 0.16.sp
    val TitleColorAlpha = 0.64f
    val BottomPadding = 8.dp
}

private object SettingsDividerDefaults {
    val Thickness = 0.64.dp
    val DividerAlpha = 0.32f
    val TopPadding = 12.dp
    val BottomPadding = 6.dp
}

/**
 * Spacer 30.dp height
 */
@Composable
fun SettingsSpacer(height: Dp = LocalLayoutSpacing.current.spacerHeight) {
    Spacer(modifier = Modifier.height(height))
}

/**
 * @param title The text shown on the subhead
 */
@Composable
fun SettingsSubheading(title: String = stringResource(R.string.swipe_to_show_app)) {
    Row(
        modifier = Modifier.padding(SettingsSubheadingDefaults.BottomPadding)
    ) {
        Text(
            text = title,
            color = ContentColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun SettingsSubHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(
            letterSpacing = SettingsSubHeaderDefaults.LetterSpacing,
            fontWeight = FontWeight.Medium
        ),
        color = ContentColor.copy(alpha = SettingsSubHeaderDefaults.TitleColorAlpha),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = SettingsSubHeaderDefaults.BottomPadding)
    )
}

@Composable
fun SettingsDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = SettingsDividerDefaults.TopPadding,
                bottom = SettingsDividerDefaults.BottomPadding
            ),
        thickness = SettingsDividerDefaults.Thickness,
        color = DividerColor.copy(alpha = SettingsDividerDefaults.DividerAlpha)
    )
}
