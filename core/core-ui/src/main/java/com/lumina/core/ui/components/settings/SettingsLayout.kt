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
import com.lumina.core.ui.theme.SecondaryContentColor

private object SettingsSubheadingDefaults {
    val BottomPadding = 24.dp
}

private object SettingsCardHeaderDefaults {
    val LetterSpacing = 1.0.sp
    val TitleColorAlpha = 0.75f
    val BottomPadding = 8.dp
}

private object SettingsSubHeaderDefaults {
    val LetterSpacing = 1.0.sp
    val TopPadding = 16.dp
    val BottomPadding = 4.dp
}

private object SettingsDividerDefaults {
    val LetterSpacing = 1.0.sp
    val Thickness = 2.64.dp
    val DividerAlpha = 0.64f
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
fun SettingsCardHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(
            letterSpacing = SettingsCardHeaderDefaults.LetterSpacing,
            fontWeight = FontWeight.Medium
        ),
        color = MaterialTheme.colorScheme.primary.copy(alpha = SettingsCardHeaderDefaults.TitleColorAlpha),
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = SettingsComponentsDefaults.HorizontalPadding,
                vertical = SettingsCardHeaderDefaults.BottomPadding
            )
    )
}

@Composable
fun SettingsSubHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            letterSpacing = SettingsSubHeaderDefaults.LetterSpacing,
            fontWeight = FontWeight.Normal
        ),
        color = SecondaryContentColor,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = SettingsComponentsDefaults.HorizontalPadding,
                top = SettingsSubHeaderDefaults.TopPadding,
                bottom = SettingsSubHeaderDefaults.BottomPadding
            )
    )
}

@Composable
fun SettingsDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier
            .fillMaxWidth(),
        thickness = SettingsDividerDefaults.Thickness,
        color = DividerColor.copy(alpha = SettingsDividerDefaults.DividerAlpha)
    )
}
