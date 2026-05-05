package com.lumina.feature.home.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.Motion
import com.lumina.core.ui.ThemeDimensions
import com.lumina.core.ui.theme.ContentColor
import com.lumina.feature.home.R

private object AddAppToProfileDefaults {
    const val PLACEHOLDER_TEXT_ALPHA = 0.6f
    val SpaceBetweenColumnItems = 4.dp
}

@Composable
fun AddAppToProfile(
    availableProfiles: List<Pair<String, String>>,
    initialSelectedProfileIds: Set<String>,
    onSave: (Set<String>) -> Unit,
    onCreateNewProfile: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProfileIds by remember { mutableStateOf(initialSelectedProfileIds) }

    val filteredProfileIds = remember(searchQuery, selectedProfileIds) {
        if (searchQuery.isBlank()) availableProfiles
        else availableProfiles.filter { it.second.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(
                stringResource(R.string.profile_field_placeholder),
                color = ContentColor.copy(alpha = AddAppToProfileDefaults.PLACEHOLDER_TEXT_ALPHA)
            )},
            leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = ThemeDimensions.VerticalContentPadding)
                .clip(RoundedCornerShape(ThemeDimensions.DefaultCornerRadius)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = ThemeDimensions.ExpandedContentMaxHeight)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AddAppToProfileDefaults.SpaceBetweenColumnItems)
        ) {
            filteredProfileIds.forEach { profile ->
                val (id, name) = profile
                val isSelectedProfile = selectedProfileIds.contains(id)

                val checkAlpha by animateFloatAsState(
                    targetValue = if (isSelectedProfile) 1f else 0f,
                    animationSpec = tween(Motion.SHEET_TRANSITION_MS),
                    label = "check_alpha"
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(ThemeDimensions.DefaultCornerRadius))
                        .clickable {
                            selectedProfileIds = if (isSelectedProfile) selectedProfileIds - id
                            else selectedProfileIds + id
                        },
                    color = if (isSelectedProfile) MaterialTheme.colorScheme.secondaryContainer
                        else Color.Transparent,
                    contentColor = if (isSelectedProfile) MaterialTheme.colorScheme.onSecondaryContainer
                        else ContentColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = ThemeDimensions.RowVerticalPadding,
                                horizontal = ThemeDimensions.RowHorizontalPadding
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, style = MaterialTheme.typography.bodySmall)

                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = stringResource(R.string.profile_selected_label),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.alpha(checkAlpha)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(AddAppToProfileDefaults.SpaceBetweenColumnItems))
        }

        HorizontalDivider(
            modifier = Modifier.padding(bottom = ThemeDimensions.DividerBottomPadding),
            color = MaterialTheme.colorScheme.surfaceVariant
        )

        TextButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = ThemeDimensions.InLineButtonTopPadding),
            onClick = { onSave(selectedProfileIds) }
        ) {
            Text(stringResource(R.string.profile_save_label), color = ContentColor)
        }

        TextButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = ThemeDimensions.InLineButtonTopPadding),
            onClick = onCreateNewProfile
        ) {
            Text(stringResource(R.string.profile_create_new_label), color = ContentColor)
        }
    }
}
