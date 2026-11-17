package com.josense.locktask.ui.page.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.josense.locktask.R
import com.josense.locktask.ui.components.TaskCreateButton
import com.josense.locktask.ui.components.input.KeyboardInputSheet
import com.josense.locktask.ui.theme.PageGap
import com.josense.locktask.ui.theme.extendedTypography
import org.koin.androidx.compose.koinViewModel

private val INPUT_HORIZONTAL_PADDING = 12.dp

@Composable
fun CreateTaskSheet(onDismiss: () -> Unit, onCreated: (Int) -> Unit) {
    // Each insertion into composition owns one creation session, including its ViewModel.
    val owner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(owner) {
        onDispose { owner.viewModelStore.clear() }
    }
    val viewModel: CreateTaskViewModel = koinViewModel(viewModelStoreOwner = owner)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnCreated by rememberUpdatedState(onCreated)
    val taskId = (state.submission as? Submission.Succeeded)?.taskId
    LaunchedEffect(taskId) {
        if (taskId != null) {
            currentOnCreated(taskId)
        }
    }
    KeyboardInputSheet(
        onDismissRequest = onDismiss,
        dismissEnabled = state.submission != Submission.Saving && state.submission !is Submission.Succeeded,
        title = stringResource(R.string.create_task),
    ) {
        CreateTaskInputs(state, initialFocusModifier, viewModel::onAction)
        if (state.submission == Submission.Failed) {
            Text(
                text = stringResource(R.string.create_task_failed),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = PageGap),
            )
        }
        TaskFuncBtnRow(
            enableCreate =
            state.title.isNotBlank() && state.submission != Submission.Saving &&
                state.submission !is Submission.Succeeded,
            needLockNotify = state.needLockNotify,
            updateLockNotify = { viewModel.onAction(CreateTaskAction.ToggleLockNotify) },
            onCreated = { viewModel.onAction(CreateTaskAction.Submit) },
        )
    }
}

@Composable
internal fun CreateTaskInputs(
    state: CreateTaskState,
    initialFocusModifier: Modifier,
    onAction: (CreateTaskAction) -> Unit,
) {
    val descriptionFocus = remember { FocusRequester() }
    val readOnly = state.submission == Submission.Saving || state.submission is Submission.Succeeded
    TaskTitleInputField(
        currentInput = state.title,
        updateInput = { onAction(CreateTaskAction.TitleChanged(it)) },
        modifier = initialFocusModifier.testTag("create_task_title"),
        readOnly = readOnly,
        onNext = { descriptionFocus.requestFocus() },
    )
    BasicTextField(
        value = state.description,
        onValueChange = { onAction(CreateTaskAction.DescriptionChanged(it)) },
        readOnly = readOnly,
        minLines = 2,
        maxLines = 8,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        modifier = Modifier.focusRequester(descriptionFocus)
            .testTag("create_task_description")
            .fillMaxWidth()
            .padding(horizontal = PageGap)
            .padding(bottom = 12.dp)
            .padding(horizontal = INPUT_HORIZONTAL_PADDING, vertical = 10.dp),
        decorationBox = { innerTextField ->
            Box {
                if (state.description.isEmpty()) {
                    Text(
                        stringResource(R.string.add_task_description_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
fun TaskTitleInputField(
    currentInput: String,
    updateInput: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    onNext: () -> Unit = {},
) {
    BasicTextField(
        value = currentInput,
        onValueChange = updateInput,
        readOnly = readOnly,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { onNext() }),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        modifier = modifier
            .onPreviewKeyEvent {
                if (it.key == Key.Enter || it.key == Key.NumPadEnter) {
                    if (it.type == KeyEventType.KeyUp) onNext()
                    true
                } else {
                    false
                }
            }
            .fillMaxWidth()
            .padding(horizontal = PageGap + INPUT_HORIZONTAL_PADDING, vertical = 6.dp),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(modifier = Modifier.weight(1F)) {
                    if (currentInput.isEmpty()) {
                        Text(
                            text = stringResource(R.string.add_task_title_placeholder),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.extendedTypography.placeholderLarge,
                        )
                    }

                    innerTextField()
                }
            }
        },
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
