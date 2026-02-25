package com.lumina.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.theme.CardContainerColor
import com.lumina.core.ui.theme.ContentColor
import com.lumina.feature.home.R

private object FirstTimeHelpDefaults {
    val TopRowPadding = 25.dp
    val TopRowBottomPadding = 15.dp
    val BottomRowPadding = 25.dp
    val BottomRowTopPadding = 0.dp
    val IconSpacing = 5.dp
}

@Composable
fun FirstTimeHelp() {
    Box(
        Modifier.clip(
            MaterialTheme.shapes.extraLarge
        )
    ) {
        Column(
            Modifier.background(CardContainerColor)
        ) {
            Row(
                Modifier
                    .padding(
                        start = FirstTimeHelpDefaults.TopRowPadding,
                        top = FirstTimeHelpDefaults.TopRowPadding,
                        end = FirstTimeHelpDefaults.TopRowPadding,
                        bottom = FirstTimeHelpDefaults.TopRowBottomPadding
                    )
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    null,
                    Modifier.align(Alignment.CenterVertically),
                    tint = ContentColor
                )
                Spacer(Modifier.width(FirstTimeHelpDefaults.IconSpacing))
                Text(
                    stringResource(R.string.swipe_for_all_apps),
                    modifier = Modifier,
                    color = ContentColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                Modifier
                    .padding(
                        start = FirstTimeHelpDefaults.BottomRowPadding,
                        top = FirstTimeHelpDefaults.BottomRowTopPadding,
                        end = FirstTimeHelpDefaults.BottomRowPadding,
                        bottom = FirstTimeHelpDefaults.BottomRowPadding
                    )
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.Default.Settings,
                    null,
                    Modifier.align(Alignment.CenterVertically),
                    tint = ContentColor
                )
                Spacer(Modifier.width(FirstTimeHelpDefaults.IconSpacing))
                Text(
                    stringResource(R.string.hold_for_settings),
                    modifier = Modifier,
                    color = ContentColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
