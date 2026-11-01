package com.josense.locktask.ui.page.task_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.josense.locktask.R
import com.josense.locktask.domain.model.Task
import java.text.DateFormat
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailRoot(
    viewModel: TaskDetailViewModel = koinViewModel(),
    onBackClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val task = state.task
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
                title = { Text(stringResource(R.string.task_detail)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Sharp.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    if (task != null) {
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
                        DebugSaveFailureMenu(state.isSaving.not())
                    }
                },
            )
        },
        bottomBar = {
            task?.let {
                LockNotifyToggleBar(
                    checked = it.needLockNotify,
                    enabled = state.isSaving.not() && it.isDeleted.not(),
                    onCheckedChange = { enabled ->
                        viewModel.onAction(
                            TaskDetailAction.SetLockNotify(enabled)
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
            TaskDetailContent(current, Modifier.padding(padding))
        }
    }
}

@Composable
private fun TaskDetailContent(task: Task, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) {
        DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM,
            DateFormat.SHORT,
            locale,
        )
    }
    SelectionContainer {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = formatter.format(task.updateAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            Text(
                text = task.content.ifBlank {
                    stringResource(R.string.task_detail_no_description)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = if (task.content.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}
