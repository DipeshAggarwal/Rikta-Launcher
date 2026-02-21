package com.lumina.core.ui.components.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.R
import com.lumina.core.ui.theme.primaryContentColor

@Composable
fun BottomSheetAppHeader() {
    Spacer(modifier = Modifier.height(140.dp))
    Text(
        text = stringResource(id = R.string.all_apps),
        color = primaryContentColor,
        style = MaterialTheme.typography.titleMedium
    )
}
