package com.josense.locktask.ui.page.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import com.josense.locktask.R
import com.josense.locktask.domain.model.Task
import com.josense.locktask.ui.components.TaskCreateButton
import com.josense.locktask.ui.components.input.KeyboardInputSheet
import com.josense.locktask.ui.theme.PageGap
import com.josense.locktask.ui.theme.TextFieldPadding
import com.josense.locktask.ui.theme.extendedTypography
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateTaskSheet(onDismiss: () -> Unit) {
    val viewModel: CreateTaskViewModel = koinViewModel()
    KeyboardInputSheet(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.create_task),
    ) {
        var textInput by remember { mutableStateOf("") }
        var needLockNotify by remember { mutableStateOf(true) }

        TaskTitleInputField(
            currentInput = textInput,
            updateInput = { textInput = it },
            modifier = initialFocusModifier,
        )
        TaskFuncBtnRow(
            enableCreate = textInput.isNotBlank(),
            needLockNotify = needLockNotify,
            updateLockNotify = { needLockNotify = !needLockNotify },
            onCreated = {
                viewModel.createTask(Task(title = textInput, needLockNotify = needLockNotify))
                dismiss()
            },
        )
    }
}

@Composable
fun TaskTitleInputField(
    currentInput: String,
    updateInput: (String) -> Unit,
    modifier: Modifier = Modifier,
) {

    BasicTextField(
        value = currentInput,
        onValueChange = updateInput,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        modifier = modifier
            .fillMaxWidth()
            .padding(TextFieldPadding),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1F)) {
                    if (currentInput.isEmpty()) {
                        Text(
                            text = stringResource(R.string.add_task_title_placeholder),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
    ) {
        TaskCreateButton(
            enabled = enableCreate,
            needLockNotify = needLockNotify,
            onModeSelected = { if (it != needLockNotify) updateLockNotify() },
            onCreate = onCreated,
        )
    }
}
