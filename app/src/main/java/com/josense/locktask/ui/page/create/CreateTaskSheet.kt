package com.josense.locktask.ui.page.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.josense.locktask.R
import com.josense.locktask.domain.model.Task
import com.josense.locktask.ui.components.LockNotifyIconBtnWithTooltip
import com.josense.locktask.ui.theme.PageGap
import com.josense.locktask.ui.theme.TextFieldPadding
import com.josense.locktask.ui.theme.extendedTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
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
                updateInput = { textInput = it },
                sheetState = sheetState,
            )

            // 功能按钮区
            TaskFuncBtnRow(
                enableCreate = textInput.isNotBlank(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTitleInputField(
    currentInput: String,
    updateInput: (String) -> Unit,
    sheetState: SheetState,
) {

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(sheetState, focusRequester) {
        snapshotFlow { !sheetState.isAnimationRunning && sheetState.currentValue == SheetValue.Expanded }
            .first { it }

        focusRequester.requestFocus()

    }
    BasicTextField(
        value = currentInput,
        onValueChange = updateInput,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        textStyle = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(TextFieldPadding)
            .focusRequester(focusRequester),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1F)) {
                    if (currentInput.isEmpty()) {
                        Text(
                            text = stringResource(R.string.add_task_title_placeholder),
                            style = MaterialTheme.extendedTypography.placeholderLarge
                        )
                    }

                    innerTextField()
                }
            }

        }
    )
}

@Composable
fun TaskFuncBtnRow(
    enableCreate: Boolean,
    needLockNotify: Boolean,
    updateLockNotify: () -> Unit,
    onCreated: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = PageGap),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        LockNotifyIconBtnWithTooltip(
            enable = true,
            needLockNotify = needLockNotify,
            updateLockNotify = updateLockNotify
        )

        Button(
            enabled = enableCreate,
            onClick = onCreated
        ) {
            Text(text = stringResource(R.string.create_task))
        }
    }
}