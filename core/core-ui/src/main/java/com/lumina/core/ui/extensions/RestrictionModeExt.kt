package com.lumina.core.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.lumina.core.model.RestrictionMode
import com.lumina.core.ui.R

@Composable
fun RestrictionMode.displayName(): String = stringResource(
    when (this) {
        RestrictionMode.STRICT -> R.string.restriction_strict
        RestrictionMode.BALANCED -> R.string.restriction_balanced
        RestrictionMode.RELAXED -> R.string.restriction_relaxed
        RestrictionMode.CUSTOM -> R.string.restriction_custom
        RestrictionMode.LOCKED -> R.string.restriction_locked
    }
)
