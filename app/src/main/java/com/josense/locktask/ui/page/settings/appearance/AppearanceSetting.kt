package com.josense.locktask.ui.page.settings.appearance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.josense.locktask.R
import com.josense.locktask.domain.settings.AppearanceMode
import com.josense.locktask.ui.page.settings.SettingsEntry
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun AppearanceSetting() {
    val model: AppearanceViewModel = koinViewModel()
    val state by model.state.collectAsStateWithLifecycle()
    SettingsEntry(
        title = stringResource(R.string.settings_appearance),
        description = stringResource(state.mode.label),
        icon = Icons.Outlined.Palette,
        onClick = { model.onAction(AppearanceAction.ShowPicker) },
    )
    if (state.showPicker) {
        AlertDialog(
            onDismissRequest = { model.onAction(AppearanceAction.DismissPicker) },
            title = { Text(stringResource(R.string.settings_appearance)) },
            text = {
                Column(Modifier.selectableGroup()) {
                    AppearanceMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier.fillMaxWidth().selectable(
                                selected = state.mode == mode,
                                role = Role.RadioButton,
                                onClick = { model.onAction(AppearanceAction.SelectMode(mode)) },
                            ).padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = state.mode == mode, onClick = null)
                            Text(stringResource(mode.label), Modifier.padding(start = 16.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { model.onAction(AppearanceAction.DismissPicker) }) {
                    Text(stringResource(R.string.settings_cancel))
                }
            },
        )
    }
}

private val AppearanceMode.label: Int
    get() = when (this) {
        AppearanceMode.System -> R.string.settings_appearance_system
        AppearanceMode.Light -> R.string.settings_appearance_light
        AppearanceMode.Dark -> R.string.settings_appearance_dark
    }
