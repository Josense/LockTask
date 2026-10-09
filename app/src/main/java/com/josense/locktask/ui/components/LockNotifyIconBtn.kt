package com.josense.locktask.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.josense.locktask.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockNotifyIconBtnWithTooltip(
    enable: Boolean,
    needLockNotify: Boolean,
    updateLockNotify: (Boolean) -> Unit,
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
    updateLockNotify: (Boolean) -> Unit = {},
) {
    FilledIconToggleButton(
        checked = needLockNotify,
        onCheckedChange = updateLockNotify,
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
        )
    }
}