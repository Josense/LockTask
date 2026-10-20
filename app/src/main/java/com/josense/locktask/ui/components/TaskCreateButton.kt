package com.josense.locktask.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.josense.locktask.R

@Composable
fun TaskCreateButton(
    enabled: Boolean,
    needLockNotify: Boolean,
    onModeSelected: (Boolean) -> Unit,
    onCreate: () -> Unit,
) {
    val currentMode = stringResource(
        if (needLockNotify) R.string.lock_notify_label else R.string.in_app_only_label
    )
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Button(
            onClick = onCreate,
            enabled = enabled,
            shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp, topEnd = 4.dp, bottomEnd = 4.dp),
        ) {
            AnimatedContent(
                targetState = needLockNotify,
                transitionSpec = {
                    val direction = if (targetState) -1 else 1
                    slideInVertically { direction * it } togetherWith
                        slideOutVertically { -direction * it }
                },
                label = "createMode",
            ) { lockNotify ->
                Text(stringResource(if (lockNotify) R.string.create_with_lock_notify else R.string.create_in_app_only))
            }
        }
        // 直接切换状态，不打开弹层，保留输入框焦点和软键盘。
        Button(
            onClick = { onModeSelected(!needLockNotify) },
            modifier = Modifier.width(48.dp).semantics { stateDescription = currentMode },
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 24.dp, bottomEnd = 24.dp),
        ) {
            Icon(
                Icons.Default.SwapVert,
                contentDescription = stringResource(
                    if (needLockNotify) R.string.switch_to_in_app_only else R.string.switch_to_lock_notify
                ),
            )
        }
    }
}
