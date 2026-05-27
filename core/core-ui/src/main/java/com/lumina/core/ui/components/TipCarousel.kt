package com.lumina.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.ThemeTokens

data class TipContent(
    val text: String,
    val icon: ImageVector = Icons.Outlined.TouchApp
)

@Composable
fun TipCarousel(
    tips: List<TipContent>,
    modifier: Modifier = Modifier
) {
    if (tips.isEmpty()) return
    if (tips.size == 1) {
        TipBanner(tipText = tips[0].text, modifier = modifier, iconVector = tips[0].icon)
        return
    }

    val pagerState = rememberPagerState(pageCount = { tips.size })

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = ThemeTokens.Alpha.Medium)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ThemeTokens.Spacing.ExtraLarge,
                    vertical = ThemeTokens.Spacing.Large
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                pageSpacing = ThemeTokens.Spacing.Large
            ) { page ->
                val tip = tips[page]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = tip.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = ThemeTokens.Alpha.Heavy
                        ),
                        modifier = Modifier.size(ThemeTokens.Icon.BannerCloseIconSize)
                    )
                    Spacer(modifier = Modifier.width(ThemeTokens.Spacing.Medium))

                    Text(
                        text = tip.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = ThemeTokens.Alpha.Heavy
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(modifier = Modifier.height((ThemeTokens.Spacing.Large)))
            Row(
                horizontalArrangement = Arrangement.spacedBy(ThemeTokens.Spacing.Small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(tips.size) { index ->
                    val selected = pagerState.currentPage == index
                    val color by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = ThemeTokens.Alpha.Light
                            ),
                        label = "tip_carousel_dot_color"
                    )

                    Box(
                        modifier = Modifier
                            .size(if (selected) 8.dp else 6.dp)
                            .background(color, CircleShape)
                    )
                }
            }
        }
    }
}
