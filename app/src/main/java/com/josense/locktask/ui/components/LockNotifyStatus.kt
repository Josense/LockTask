package com.josense.locktask.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.josense.locktask.R
import com.josense.locktask.ui.theme.customDisabled

@Composable
fun LockNotifyStatus(needLockNotify: Boolean) {
    Icon(
        painter = painterResource(R.drawable.ic_pinboard_unread),
        contentDescription = stringResource(
            if (needLockNotify) R.string.lock_notify_enabled else R.string.lock_notify_disabled
        ),
        modifier = Modifier.padding(12.dp).size(24.dp),
        tint = if (needLockNotify) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.customDisabled,
    )
}
