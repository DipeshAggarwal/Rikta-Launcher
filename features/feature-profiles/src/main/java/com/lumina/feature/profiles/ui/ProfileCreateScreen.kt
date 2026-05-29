package com.lumina.feature.profiles.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumina.core.model.ProfilePreset
import com.lumina.core.ui.Motion
import com.lumina.core.ui.ThemeTokens
import com.lumina.core.ui.components.ElevatedButton
import com.lumina.core.ui.components.PrimaryButton
import com.lumina.core.ui.components.StandardListScaffold
import com.lumina.core.ui.extensions.accentColourRes
import com.lumina.core.ui.extensions.description
import com.lumina.core.ui.extensions.displayName
import com.lumina.core.ui.extensions.icon
import com.lumina.core.ui.extensions.subtitle
import com.lumina.feature.profiles.ProfileManageUiState
import com.lumina.feature.profiles.ProfileManageViewModel
import com.lumina.feature.profiles.ProfilesDefaults
import com.lumina.feature.profiles.R
import com.lumina.feature.profiles.ui.component.InLineTextField
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun ProfileCreateScreen(
    viewModel: ProfileManageViewModel = hiltViewModel(),
    onCustomiseSettings: () -> Unit,
    onProfileCreated: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val createState = uiState as? ProfileManageUiState.Ready ?: return

    var selectedPreset by rememberSaveable { mutableStateOf<ProfilePreset?>(null) }
    var nameEdited by rememberSaveable { mutableStateOf(false) }
    var descriptionEdited by rememberSaveable { mutableStateOf(false) }

    val isCustom = selectedPreset == ProfilePreset.CUSTOM
    val listState = rememberLazyListState()
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        snapshotFlow { imeInsets.getBottom(density = density) }
            .distinctUntilChanged()
            .filter { it == 0 }
            .collect {
                listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
            }
    }

    val createButtonLabel = if (selectedPreset == null) stringResource(R.string.create_profile_button_default)
        else stringResource(
            R.string.create_profile_button_selected,
        createState.draftProfile.name.ifBlank { (selectedPreset as ProfilePreset).displayName() }
        )

    // verticalArrangement = Arrangement.spacedBy(ThemeTokens.Spacing.Small)
    StandardListScaffold(
        title = stringResource(R.string.create_profile_headers),
        onBack = onBack,
        listModifier = Modifier.imePadding(),
        verticalColumnSpacing = ThemeTokens.Spacing.Small
    ) {
        item(key = "create_profile_info") {
            Text(
                text = stringResource(R.string.create_profile_info),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item(key = "create_profile_info_spacer") {
            Spacer(modifier = Modifier.height(ThemeTokens.Spacing.Large))
        }

        items(
            count = ProfilePreset.entries.size,
            key = { ProfilePreset.entries[it].name }
        ) { index ->
            val preset = ProfilePreset.entries[index]
            val isSelected = selectedPreset == preset

            val name = preset.displayName()
            val description = preset.description()

            PresetCard(
                preset = preset,
                isSelected = isSelected,
                onClick = {
                    selectedPreset = preset
                    viewModel.applyPreset(preset)
                    if (!nameEdited) viewModel.updateName(name)
                    if (!descriptionEdited) viewModel.updateDescription(description)
                }
            )
        }
        item(key = "create_profile_preset_spacer") {
            Spacer(modifier = Modifier.height(ThemeTokens.Spacing.Large))
        }

        item(key = "create_profile_details_field") {
            ProfileDetailsContainer(
                title = stringResource(R.string.create_profile_add_details),
                name = createState.draftProfile.name,
                description = createState.draftProfile.description ?: "",
                onNameChange = { value ->
                    nameEdited = value.isNotEmpty()
                    viewModel.updateName(value)
                },
                onDescriptionChange = { value ->
                    descriptionEdited = value.isNotEmpty()
                    viewModel.updateDescription(value)
                }
            )
        }
        item(key = "create_profile_details_spacer") {
            Spacer(modifier = Modifier.height(ThemeTokens.Spacing.Large))
        }

        item(key = "create_profile_review") {
            ElevatedButton(
                title = stringResource(R.string.create_button_review),
                subtitle = stringResource(R.string.create_button_review_subtitle),
                enabled = !isCustom,
                leadingIcon = null,
                trailingIcon = Icons.Outlined.ChevronRight,
                onClick = onCustomiseSettings
            )
        }
        item(key = "create_profile_review_spacer") {
            Spacer(modifier = Modifier.height(ThemeTokens.Spacing.Medium))
        }

        item(key = "create_profile_button") {
            val name = selectedPreset?.displayName() ?: ""
            val description = selectedPreset?.description() ?: ""

            PrimaryButton(
                title = createButtonLabel,
                enabled = selectedPreset != null && createState.draftProfile.name.isNotBlank() && !createState.isSaving,
                onClick = {
                    if (isCustom) {
                        onCustomiseSettings()
                    } else {
                        if (!nameEdited && selectedPreset != null && createState.draftProfile.name.isBlank()) {
                            viewModel.updateName(name)
                        }
                        if (!descriptionEdited && selectedPreset != null && createState.draftProfile.description.isNullOrBlank()) {
                            viewModel.updateDescription(description)
                        }
                        viewModel.saveProfile()
                        onProfileCreated()
                    }
                }
            )
        }
    }
}

@Composable
private fun PresetCard(
    preset: ProfilePreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = preset.accentColourRes()
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(Motion.SINGlE_ELEMENT_TRANSITION_MS),
        label = "preset_border_${preset.name}"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = ThemeTokens.Alpha.Light)
            else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(Motion.SINGlE_ELEMENT_TRANSITION_MS),
        label = "preset_container_${preset.name}"
    )
    val subtitleColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(Motion.SINGlE_ELEMENT_TRANSITION_MS),
        label = "preset_description_${preset.name}"
    )

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClick = onClick)
                .padding(
                    horizontal = ThemeTokens.Spacing.ExtraLarge,
                    vertical = ThemeTokens.Spacing.Medium
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = isSelected,
                transitionSpec = {
                    fadeIn(tween(Motion.SINGlE_ELEMENT_TRANSITION_MS)) togetherWith
                            fadeOut(tween(Motion.SINGlE_ELEMENT_TRANSITION_MS))
                },
                label = "preset_icon_${preset.name}"
            ) { selected ->
                Icon(
                    imageVector = if (selected) Icons.Outlined.Check else preset.icon(),
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(ThemeTokens.Icon.InlineSize)
                )
            }
            Spacer(modifier = Modifier.width(ThemeTokens.Spacing.Large))

            Text(
                text = preset.displayName(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(ThemeTokens.Spacing.Large))
            Text(
                text = preset.subtitle(),
                style = MaterialTheme.typography.bodyMedium,
                color = subtitleColor
            )
        }
    }
}

@Composable
private fun ProfileDetailsContainer(
    title: String,
    name: String,
    description: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var containerFocused by rememberSaveable { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (containerFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(Motion.SINGlE_ELEMENT_TRANSITION_MS),
        label = "details_border"
    )

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column{
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    start = ThemeTokens.Spacing.ExtraLarge,
                    end = ThemeTokens.Spacing.ExtraLarge,
                    top = ThemeTokens.Spacing.Large
                )
            )

            InLineTextField(
                label = stringResource(R.string.create_profile_add_name_label),
                value = name,
                maxLength = ProfilesDefaults.NAME_MAX_CHARACTERS,
                maxLines = 1,
                onValueChange = {
                    if (it.length <= ProfilesDefaults.NAME_MAX_CHARACTERS) onNameChange(it)
                },
                onFocusChange = { containerFocused = it },
                imeAction = ImeAction.Next
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = ThemeTokens.Spacing.ExtraLarge),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            InLineTextField(
                label = stringResource(R.string.create_profile_add_description_label),
                value = description,
                maxLength = ProfilesDefaults.DESCRIPTION_MAX_CHARACTERS,
                maxLines = 3,
                onValueChange = {
                    if (it.length <= ProfilesDefaults.DESCRIPTION_MAX_CHARACTERS) onDescriptionChange(it)
                },
                onFocusChange = { containerFocused = it },
                imeAction = ImeAction.Done
            )
        }
    }
}
