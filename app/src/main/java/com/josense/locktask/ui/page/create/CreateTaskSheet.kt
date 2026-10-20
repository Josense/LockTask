package com.josense.locktask.ui.page.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import com.josense.locktask.R
import com.josense.locktask.ui.components.TaskCreateButton
import com.josense.locktask.ui.components.input.KeyboardInputSheet
import com.josense.locktask.ui.theme.PageGap
import com.josense.locktask.ui.theme.TextFieldPadding
import com.josense.locktask.ui.theme.extendedTypography
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateTaskSheet(
    onDismiss: () -> Unit,
    onCreated: (Int) -> Unit,
) {
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
        TaskTitleInputField(
            currentInput = state.title,
            updateInput = { viewModel.onAction(CreateTaskAction.TitleChanged(it)) },
            modifier = initialFocusModifier,
        )
        if (state.submission == Submission.Failed) {
            Text(
                text = stringResource(R.string.create_task_failed),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = PageGap),
            )
        }
        TaskFuncBtnRow(
            enableCreate = state.title.isNotBlank() && state.submission != Submission.Saving && state.submission !is Submission.Succeeded,
            needLockNotify = state.needLockNotify,
            updateLockNotify = { viewModel.onAction(CreateTaskAction.ToggleLockNotify) },
            onCreated = { viewModel.onAction(CreateTaskAction.Submit) },
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
