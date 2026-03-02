package com.lumina.core.ui.components.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.R
import com.lumina.core.ui.layout.LocalLayoutSpacing
import com.lumina.core.ui.theme.ContentColor

private object SettingsSubheadingDefaults {
    val BottomPadding = 24.dp
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
