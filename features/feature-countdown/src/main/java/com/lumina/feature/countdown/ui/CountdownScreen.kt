package com.lumina.feature.countdown.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumina.core.ui.HapticUtils
import com.lumina.core.ui.Motion.SCREEN_TRANSITION_DURATION
import com.lumina.core.ui.theme.CardContainerColor
import com.lumina.core.ui.theme.ContentColor
import com.lumina.feature.countdown.CountdownUiState
import com.lumina.feature.countdown.CountdownViewModel
import com.lumina.feature.countdown.R

@Composable
fun CountdownScreen(
    isVisible: Boolean,
    onCompleted: () -> Unit,
    onCancelled: () -> Unit,
    viewModel: CountdownViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val animatedColor by infiniteTransition.animateColor(
        initialValue = colorScheme.tertiary,
        targetValue = colorScheme.primary,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientColor"
    )

    val gardientColors = listOf(
        animatedColor,
        colorScheme.primary,
        animatedColor,
    )

    val prompts = stringArrayResource(R.array.countdown_prompts)
    var selectedPrompt by remember { mutableStateOf("") }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            selectedPrompt = prompts.random()
            viewModel.start()
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            CountdownUiState.Completed -> {
                viewModel.reset()
                onCompleted()
            }
            CountdownUiState.Cancelled -> {
                viewModel.reset()
                onCancelled()
            }
            else -> Unit
        }
    }

    BackHandler(enabled = isVisible) { viewModel.cancel()}

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = gardientColors
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = selectedPrompt,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 82.dp, start = 32.dp, end = 32.dp),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(0.85f),
            textAlign = TextAlign.Center
        )

        when (uiState) {
            is CountdownUiState.Running -> {
                val running = uiState as CountdownUiState.Running
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedVisibility(
                        visible = running.showNumber,
                        enter = fadeIn(tween(SCREEN_TRANSITION_DURATION)),
                        exit = fadeOut(tween(SCREEN_TRANSITION_DURATION))
                    ) {
                        Text(
                            text = running.currentStep.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White
                        )
                    }
                }

                Button(
                    modifier = Modifier.padding(top = 128.dp),
                    colors = ButtonColors(
                        ContentColor,
                        CardContainerColor,
                        ContentColor,
                        CardContainerColor
                    ),
                    onClick = {
                        HapticUtils.performHapticFeedback(haptics)
                        viewModel.cancel()
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        "Go back",
                        tint = CardContainerColor
                    )
                }
            }

            else -> Unit
        }
    }
}
