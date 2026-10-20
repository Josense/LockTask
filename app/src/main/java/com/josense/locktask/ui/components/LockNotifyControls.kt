package com.josense.locktask.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.josense.locktask.R

@Composable
fun LockNotifyStatus(needLockNotify: Boolean) {
    Icon(
        imageVector = if (needLockNotify) Icons.Default.Notifications else Icons.Default.NotificationsOff,
        contentDescription = stringResource(
            if (needLockNotify) R.string.lock_notify_enabled else R.string.lock_notify_disabled
        ),
        modifier = Modifier.padding(12.dp),
        tint = if (needLockNotify) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
