package com.lumina.core.ui.components.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lumina.core.common.AppTheme
import com.lumina.core.ui.R
import com.lumina.core.ui.theme.displayNameRes
import com.lumina.core.ui.theme.resolveColorSeed
import com.lumina.core.ui.theme.transparentHalf

private object ThemeCardDefaults {
    const val BUTTON_WEIGHT = 1f

    val CardHeight = 72.dp

    val BorderWidth = 2.dp

    val CheckIconPadding = 10.dp
    val TitleHorizontalPadding = 24.dp
    val TitleVerticalPadding = 12.dp

    val LightDarkOverlayPaddingStart = 20.dp
    val LightDarkOverlayPaddingInner = 5.dp
    val LightDarkOverlayPaddingEnd = 20.dp
}

/**
 * Theme select card
 *
 * @param theme The theme ID number (see: Theme.kt)
 *
 * @see com.lumina.core.ui.theme.RiktaTheme
 */
@Composable
fun ThemeCard(
    theme: AppTheme,
    showLightDarkPicker: MutableState<Boolean>,
    isSelected: MutableState<Boolean>,
    isDSelected: MutableState<Boolean>,
    isLSelected: MutableState<Boolean>,
    updateLTheme: (AppTheme) -> Unit,
    updateDTheme: (AppTheme) -> Unit,
    modifier: Modifier,
    onClick: (AppTheme) -> Unit,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false
) {
    val currentShape = settingsGroupShape(
        isTopOfGroup = isTopOfGroup,
        isBottomOfGroup = isBottomOfGroup
    )
    Box(Modifier.padding(vertical = SettingsComponentsDefaults.VerticalPadding)) {
        Box(
            modifier
                .clip(
                    currentShape
                )
                .clickable {
                    onClick(theme)
                }
                .background(theme.resolveColorSeed().background)
                .height(ThemeCardDefaults.CardHeight)
        ) {
            AnimatedVisibility(
                isSelected.value && !showLightDarkPicker.value && !showLightDarkPicker.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .border(
                            ThemeCardDefaults.BorderWidth,
                            theme.resolveColorSeed().onPrimaryContainer,
                            currentShape
                        )
                ) {
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(ThemeCardDefaults.CheckIconPadding)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            "",
                            tint = theme.resolveColorSeed().onPrimaryContainer
                        )
                    }
                }
            }

            AnimatedVisibility(
                isSelected.value && !showLightDarkPicker.value, enter = fadeIn(), exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .border(
                            ThemeCardDefaults.BorderWidth,
                            theme.resolveColorSeed().onPrimaryContainer,
                            currentShape
                        )
                ) {
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(ThemeCardDefaults.CheckIconPadding)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            "",
                            tint = theme.resolveColorSeed().onPrimaryContainer
                        )
                    }
                }
            }

            AnimatedVisibility(
                isDSelected.value && !showLightDarkPicker.value, enter = fadeIn(), exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .border(
                            ThemeCardDefaults.BorderWidth,
                            theme.resolveColorSeed().onPrimaryContainer,
                            currentShape
                        )
                ) {
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(ThemeCardDefaults.CheckIconPadding)
                    ) {
                        Icon(
                            painterResource(R.drawable.dark_mode),
                            "",
                            tint = theme.resolveColorSeed().onPrimaryContainer
                        )
                    }
                }
            }

            Text(
                stringResource(theme.displayNameRes()),
                Modifier
                    .align(Alignment.Center)
                    .padding(
                        horizontal = ThemeCardDefaults.TitleHorizontalPadding,
                        vertical = ThemeCardDefaults.TitleVerticalPadding
                    ),
                theme.resolveColorSeed().onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            AnimatedVisibility(
                isLSelected.value && !showLightDarkPicker.value, enter = fadeIn(), exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .border(
                            ThemeCardDefaults.BorderWidth,
                            theme.resolveColorSeed().onPrimaryContainer,
                            currentShape
                        )
                ) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(ThemeCardDefaults.CheckIconPadding)
                    ) {
                        Icon(
                            painterResource(R.drawable.light_mode),
                            "",
                            tint = theme.resolveColorSeed().onPrimaryContainer
                        )
                    }
                }
            }

            AnimatedVisibility(showLightDarkPicker.value, enter = fadeIn(), exit = fadeOut()) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .background(transparentHalf)
                ) {
                    Button(
                        onClick = {
                            updateLTheme(theme)
                        },
                        modifier = Modifier
                            .weight(ThemeCardDefaults.BUTTON_WEIGHT)
                            .fillMaxHeight()
                            .padding(
                                start = ThemeCardDefaults.LightDarkOverlayPaddingStart,
                                top = ThemeCardDefaults.LightDarkOverlayPaddingInner,
                                end = ThemeCardDefaults.LightDarkOverlayPaddingInner,
                                bottom = ThemeCardDefaults.LightDarkOverlayPaddingInner
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.resolveColorSeed().primary,
                            contentColor = theme.resolveColorSeed().onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.light_mode))
                    }

                    Button(
                        onClick = {
                            updateDTheme(theme)
                        },
                        modifier = Modifier
                            .weight(ThemeCardDefaults.BUTTON_WEIGHT)
                            .fillMaxHeight()
                            .padding(
                                start = ThemeCardDefaults.LightDarkOverlayPaddingStart,
                                top = ThemeCardDefaults.LightDarkOverlayPaddingInner,
                                end = ThemeCardDefaults.LightDarkOverlayPaddingEnd,
                                bottom = ThemeCardDefaults.LightDarkOverlayPaddingInner
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.resolveColorSeed().primary,
                            contentColor = theme.resolveColorSeed().onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.dark_mode))
                    }
                }
            }
        }
    }
}
