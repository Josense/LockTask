package com.josense.locktask.ui.page.task_detail

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.josense.locktask.R
import com.josense.locktask.debug.DetailSaveFailure
import org.koin.compose.koinInject

@Composable
private fun DebugSaveFailureMenuItem(onDismiss: () -> Unit) {
    val failure = koinInject<DetailSaveFailure>()
    val armed by failure.armed.collectAsStateWithLifecycle()
    DropdownMenuItem(
        text = {
            Text(
                stringResource(
                    if (armed) R.string.debug_cancel_save_failure
                    else R.string.debug_arm_save_failure,
                )
            )
        },
        onClick = {
            failure.toggle()
            onDismiss()
        },
    )
}

@Composable
internal fun DebugSaveFailureMenu(enabled: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(enabled = enabled, onClick = { expanded = true }) {
            Icon(
                Icons.Rounded.MoreVert,
                contentDescription = stringResource(R.string.task_detail_more),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DebugSaveFailureMenuItem { expanded = false }
        }
    }
}
