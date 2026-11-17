package com.josense.locktask.ui.page.task_detail

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.josense.locktask.domain.model.Task
import com.josense.locktask.task.presentation.R

@Composable
internal fun TaskDetailEditor(
    task: Task,
    edit: TaskDetailEdit,
    saving: Boolean,
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    onAction: (TaskDetailAction) -> Unit,
) {
    val titleFocus = remember { FocusRequester() }
    val descriptionFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    LaunchedEffect(edit.field) {
        if (edit.field == TaskDetailField.Title) titleFocus.requestFocus() else descriptionFocus.requestFocus()
        keyboard?.show()
    }
    DisposableEffect(Unit) {
        onDispose {
            focusManager.clearFocus(force = true)
            keyboard?.hide()
        }
    }
    TaskDetailLayout(
        task = task,
        modifier = modifier.imePadding(),
        scrollState = scrollState,
        title = {
            BasicTextField(
                value = edit.title,
                onValueChange = { onAction(TaskDetailAction.TitleChanged(it)) },
                modifier = Modifier.fillMaxWidth().focusRequester(titleFocus).testTag("detail_edit_title"),
                readOnly = saving,
                textStyle = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { descriptionFocus.requestFocus() }),
            )
            if (edit.titleError) {
                Text(
                    stringResource(R.string.task_title_required),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        description = {
            BasicTextField(
                value = edit.description,
                onValueChange = { onAction(TaskDetailAction.DescriptionChanged(it)) },
                modifier = Modifier.fillMaxWidth().focusRequester(descriptionFocus).testTag("detail_edit_description"),
                readOnly = saving,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box {
                        if (edit.description.isEmpty()) {
                            Text(
                                stringResource(R.string.task_detail_no_description),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        },
    )
}
