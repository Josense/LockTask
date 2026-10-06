package com.josense.locktask.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.josense.locktask.R
import com.josense.locktask.ui.theme.PageGap
import com.josense.locktask.ui.theme.TextFieldPadding
import com.josense.locktask.ui.theme.customDisabled


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        var textInput by remember { mutableStateOf("") }
        var needLockNotify by remember { mutableStateOf(true) }
        val tooltipState = rememberTooltipState()

        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Column {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text(stringResource(R.string.add_task_title_hint)) },
                placeholder = { Text(stringResource(R.string.add_task_title_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(TextFieldPadding)
                    .focusRequester(focusRequester)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = PageGap),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
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
                    FilledIconToggleButton(
                        checked = needLockNotify,
                        onCheckedChange = { needLockNotify = !needLockNotify }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                        )
                    }
                }

                Button(onClick = {}) {
                    Text(text = stringResource(R.string.create_task))
                }
            }


            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}