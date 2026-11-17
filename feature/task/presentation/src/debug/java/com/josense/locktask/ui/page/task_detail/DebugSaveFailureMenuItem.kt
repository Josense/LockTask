package com.josense.locktask.ui.page.task_detail

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.josense.locktask.debug.DetailSaveFailure
import com.josense.locktask.task.presentation.R
import org.koin.compose.koinInject

@Composable
internal fun DebugSaveFailureMenuItem(onDismiss: () -> Unit) {
    val failure = koinInject<DetailSaveFailure>()
    val armed by failure.armed.collectAsStateWithLifecycle()
    HorizontalDivider()
    DropdownMenuItem(
        text = {
            Text(
                stringResource(
                    if (armed) {
                        R.string.debug_cancel_save_failure
                    } else {
                        R.string.debug_arm_save_failure
                    },
                ),
            )
        },
        onClick = {
            failure.toggle()
            onDismiss()
        },
    )
}
