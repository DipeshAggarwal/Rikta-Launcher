package com.lumina.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.Motion

private object PrimaryButtonDefaults {
    val Height = 56.dp
}

@Composable
fun PrimaryButton(
    title: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(PrimaryButtonDefaults.Height),
        enabled = enabled,
        shape = MaterialTheme.shapes.large
    ) {
        AnimatedContent(
            targetState = title,
            transitionSpec = {
                fadeIn(tween(Motion.SINGlE_ELEMENT_TRANSITION_MS)) togetherWith
                        fadeOut(tween(Motion.SINGlE_ELEMENT_TRANSITION_MS))
            },
            label = "primary_button_label"
        ) { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
