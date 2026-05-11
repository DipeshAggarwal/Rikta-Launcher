package com.lumina.core.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.lumina.core.model.ProfileClassification
import com.lumina.core.ui.R

@Composable
fun ProfileClassification.displayLabel(): String = buildString {
    append(mode.displayName())
    if (isCustomised) append(stringResource(R.string.mode_customised))
}
