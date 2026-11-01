package com.josense.locktask.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.josense.locktask.R

@Composable
fun LockNotifyStatus(needLockNotify: Boolean) {
    val colors = MaterialTheme.colorScheme
    Icon(
        painter = painterResource(
            if (needLockNotify) {
                R.drawable.ic_pinboard_unread_filled
            } else {
                R.drawable.ic_pinboard_unread
            },
        ),
        contentDescription = stringResource(
            if (needLockNotify) {
                R.string.lock_notify_enabled
            } else {
                R.string.lock_notify_disabled
            },
        ),
        modifier = Modifier.size(24.dp),
        tint = if (needLockNotify) colors.primary else colors.outline,
    )
}
