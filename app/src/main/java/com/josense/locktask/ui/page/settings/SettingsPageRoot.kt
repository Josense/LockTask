package com.josense.locktask.ui.page.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.josense.locktask.R
import com.josense.locktask.domain.notification.NotificationAccess
import com.josense.locktask.notification.presentation.R as NotificationR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPageRoot(
    notificationAccess: NotificationAccess,
    onNotificationSettings: () -> Unit,
    onBackgroundSettings: () -> Unit,
) {
    val notificationStatus = when (notificationAccess) {
        NotificationAccess.Available -> R.string.settings_notifications_enabled
        NotificationAccess.PermissionRequired -> NotificationR.string.notification_permission_description
        NotificationAccess.AppDisabled -> NotificationR.string.notification_app_disabled_description
        NotificationAccess.ChannelDisabled -> NotificationR.string.notification_channel_disabled_description
    }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_settings)) }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            SettingsSection(stringResource(R.string.settings_section_sync)) {
                SettingsEntry(
                    title = stringResource(R.string.settings_webdav),
                    description = stringResource(R.string.settings_webdav_description),
                    icon = Icons.Outlined.CloudSync,
                )
            }
            SettingsSection(stringResource(R.string.settings_section_general)) {
                SettingsEntry(
                    title = stringResource(R.string.settings_appearance),
                    icon = Icons.Outlined.Palette,
                )
            }
            SettingsSection(stringResource(R.string.settings_section_background)) {
                SettingsEntry(
                    title = stringResource(R.string.settings_notifications),
                    description = stringResource(notificationStatus),
                    icon = Icons.Outlined.Notifications,
                    onClick = onNotificationSettings,
                )
                SettingsEntry(
                    title = stringResource(NotificationR.string.background_reminder_settings),
                    description = stringResource(R.string.settings_background_description),
                    icon = Icons.Outlined.BatteryAlert,
                    onClick = onBackgroundSettings,
                )
            }
            SettingsSection(stringResource(R.string.settings_section_history)) {
                SettingsEntry(
                    title = stringResource(R.string.settings_cancelled_tasks),
                    icon = Icons.Outlined.Cancel,
                )
                SettingsEntry(
                    title = stringResource(R.string.settings_completed_tasks),
                    icon = Icons.Outlined.CheckCircle,
                )
            }
            SettingsSection(stringResource(R.string.settings_section_about)) {
                SettingsEntry(
                    title = stringResource(R.string.settings_check_updates),
                    icon = Icons.Outlined.SystemUpdate,
                )
                SettingsEntry(
                    title = stringResource(R.string.settings_feedback),
                    icon = Icons.Outlined.Feedback,
                )
            }
        }
    }
}
