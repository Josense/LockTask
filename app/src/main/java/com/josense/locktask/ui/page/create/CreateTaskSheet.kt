package com.josense.locktask.ui.page.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.josense.locktask.R
import com.josense.locktask.domain.model.Task
import com.josense.locktask.ui.theme.PageGap
import com.josense.locktask.ui.theme.TextFieldPadding
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
) {
    val createTaskViewModel: CreateTaskViewModel = koinViewModel()
    val coroutineScope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        var textInput by remember { mutableStateOf("") }
        var needLockNotify by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            // 正文输入区
            TaskTitleInputField(
                currentInput = textInput,
                updateInput = { textInput = it }
            )

            // 功能按钮区
            TaskFuncBtnRow(
                needLockNotify = needLockNotify,
                updateLockNotify = { needLockNotify = !needLockNotify },
                onCreated = {
                    createTaskViewModel.createTask(Task(title = textInput, needLockNotify = needLockNotify))
                    coroutineScope.launch { sheetState.hide() }
                        .invokeOnCompletion {
                            if (!sheetState.isVisible) onDismiss()
                        }
                }
            )
        }
    }
}

@Composable
fun TaskTitleInputField(
    currentInput: String,
    updateInput: (String) -> Unit,
) {

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    OutlinedTextField(
        value = currentInput,
        onValueChange = updateInput,
        label = { Text(stringResource(R.string.add_task_title_hint)) },
        placeholder = { Text(stringResource(R.string.add_task_title_placeholder)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(TextFieldPadding)
            .focusRequester(focusRequester)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFuncBtnRow(
    needLockNotify: Boolean,
    updateLockNotify: (Boolean) -> Unit,
    onCreated: () -> Unit,
) {

    val tooltipState = rememberTooltipState()
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
                onCheckedChange = updateLockNotify
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                )
            }
        }

        Button(onClick = onCreated) {
            Text(text = stringResource(R.string.create_task))
        }
    }
}