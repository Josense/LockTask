package com.josense.locktask.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.josense.locktask.R
import com.josense.locktask.ui.theme.customDisabled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockNotifyIconBtnWithTooltip(
    enable: Boolean,
    needLockNotify: Boolean,
    updateLockNotify: () -> Unit,
) {
    val tooltipState = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip(
                containerColor = MaterialTheme.colorScheme.inverseSurface
            ) {
                Text(stringResource(R.string.add_task_lock_screen_btn_tooltip))
            }
        },
        state = tooltipState,
    ) {
        LockNotifyIconBtn(needLockNotify, updateLockNotify)
    }
}


@Composable
fun LockNotifyIconBtn(
    needLockNotify: Boolean,
    updateLockNotify: () -> Unit = {},
) {

    IconButton(
        onClick = updateLockNotify,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.lock_notify_icon),
            contentDescription = null,
            tint = if (needLockNotify) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.customDisabled,
        )
    }
}