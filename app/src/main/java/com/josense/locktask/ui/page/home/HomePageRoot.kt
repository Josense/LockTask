package com.josense.locktask.ui.page.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.josense.locktask.ui.notification.NotificationDelegate
import com.josense.locktask.ui.notification.HomeEffect
import com.josense.locktask.ui.notification.AccessNotice
import com.josense.locktask.ui.notification.taskCreated
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collectLatest
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.koin.androidx.compose.koinViewModel
import com.josense.locktask.R
import com.josense.locktask.domain.model.Task
import com.josense.locktask.ui.page.create.CreateTaskSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageRoot(
    onTaskClick: (Task) -> Unit,
    notifications: NotificationDelegate? = null,
    viewModel: HomeViewModel = koinViewModel(),
) {

    val homeState by viewModel.state.collectAsStateWithLifecycle()
    notifications?.HomeEffect(homeState, viewModel::onAction)
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val removedMessage = stringResource(R.string.task_removed)
    val actionLabel = stringResource(R.string.undo)
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.removals.collectLatest { task ->
                val result = snackbarHostState.showSnackbar(
                    message = removedMessage.format(task.title),
                    actionLabel = actionLabel,
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.onAction(HomeAction.UndoRemoval(task))
                }
            }
        }
    }
    var showAddTaskSheet by remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = {
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = stringResource(R.string.app_description),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            })
        },
        floatingActionButton =  {
            FloatingActionButton(onClick = { showAddTaskSheet = !showAddTaskSheet }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            notifications?.AccessNotice(homeState)
            HomeScreen(
                Modifier.weight(1f),
                onTaskClick,
                homeViewModel = viewModel,
            )
        }
        if (showAddTaskSheet) {
            CreateTaskSheet(
                onDismiss = { showAddTaskSheet = false },
                onCreated = { taskId ->
                    notifications?.taskCreated(taskId)
                    viewModel.onAction(HomeAction.AnchorTask(taskId))
                    showAddTaskSheet = false
                },
            )
        }
    }
}
