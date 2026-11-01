package com.josense.locktask.ui.page.task_detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.josense.locktask.R
import com.josense.locktask.ui.components.LockNotifyToggleBar
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskDetailRoot(viewModel: TaskDetailViewModel = koinViewModel(), onBackClick: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val task = state.task
    val editing = state.edit != null
    val onBack = {
        if (editing) viewModel.onAction(TaskDetailAction.FinishEditing) else onBackClick()
    }
    BackHandler(enabled = editing) { onBack() }
    val imeVisible by rememberUpdatedState(WindowInsets.isImeVisible)
    // Android may let the IME consume Back before the activity receives it.
    LaunchedEffect(editing) {
        if (editing) {
            var keyboardWasVisible = false
            snapshotFlow { imeVisible }.collect { visible ->
                val hiddenAfterShowing = keyboardWasVisible && visible.not()
                keyboardWasVisible = visible
                if (hiddenAfterShowing) viewModel.onAction(TaskDetailAction.FinishEditing)
            }
        }
    }
    LaunchedEffect(state.isFinished) {
        if (state.isFinished) onBackClick()
    }
    val snackbar = remember { SnackbarHostState() }
    val failureMessage = stringResource(R.string.task_detail_save_failed)
    LaunchedEffect(state.saveFailed) {
        if (state.saveFailed) {
            snackbar.showSnackbar(failureMessage)
            viewModel.onAction(TaskDetailAction.DismissSaveError)
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (editing) R.string.task_detail_editing else R.string.task_detail)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Sharp.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    if (editing) {
                        TextButton(
                            enabled = state.isSaving.not(),
                            onClick = { viewModel.onAction(TaskDetailAction.FinishEditing) },
                        ) {
                            Text(
                                stringResource(
                                    if (state.isSaving) R.string.task_detail_saving else R.string.task_detail_save,
                                ),
                            )
                        }
                    } else if (task != null) {
                        IconButton(
                            enabled = state.isSaving.not(),
                            onClick = {
                                viewModel.onAction(TaskDetailAction.Complete)
                            },
                        ) {
                            Icon(
                                Icons.Outlined.Done,
                                contentDescription =
                                stringResource(R.string.complete),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        TaskDetailMoreMenu(
                            enabled = state.isSaving.not() &&
                                task.isDeleted.not(),
                            onDelete = {
                                viewModel.onAction(TaskDetailAction.Delete)
                            },
                        )
                    }
                },
            )
        },
        bottomBar = {
            task?.let {
                LockNotifyToggleBar(
                    checked = it.needLockNotify,
                    enabled = editing.not() && state.isSaving.not() && it.isDeleted.not(),
                    onCheckedChange = { enabled ->
                        viewModel.onAction(
                            TaskDetailAction.SetLockNotify(enabled),
                        )
                    },
                )
            }
        },
    ) { padding ->
        val current = task
        if (current == null) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.no_task))
            }
        } else {
            val edit = state.edit
            if (edit != null) {
                TaskDetailEditor(
                    current,
                    edit,
                    state.isSaving,
                    Modifier.padding(padding).consumeWindowInsets(padding),
                    scrollState,
                    viewModel::onAction,
                )
            } else {
                TaskDetailContent(current, Modifier.padding(padding), scrollState) {
                    viewModel.onAction(TaskDetailAction.StartEditing(it))
                }
            }
        }
    }
}
