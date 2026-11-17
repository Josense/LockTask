package com.josense.locktask.ui.page.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState())) {
            ListItem(
                modifier = Modifier.clickable(onClick = onNotificationSettings),
                headlineContent = { Text(stringResource(R.string.settings_notifications)) },
                supportingContent = { Text(stringResource(notificationStatus)) },
                leadingContent = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
                trailingContent = { Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null) },
            )
            ListItem(
                modifier = Modifier.clickable(onClick = onBackgroundSettings),
                headlineContent = { Text(stringResource(NotificationR.string.background_reminder_settings)) },
                supportingContent = { Text(stringResource(R.string.settings_background_description)) },
                leadingContent = { Icon(Icons.Outlined.BatteryAlert, contentDescription = null) },
                trailingContent = { Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null) },
            )
        }
    }
}
