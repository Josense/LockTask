package com.josense.locktask.ui.page.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.josense.locktask.R

@Composable
internal fun SyncSettingsSection() {
    SettingsSection(stringResource(R.string.settings_section_sync)) {
        SettingsEntry(
            title = stringResource(R.string.settings_webdav),
            description = stringResource(R.string.settings_webdav_description),
            icon = Icons.Outlined.CloudSync,
        )
    }
}
