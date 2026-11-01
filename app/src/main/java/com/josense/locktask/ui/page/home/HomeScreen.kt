package com.josense.locktask.ui.page.home

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.josense.locktask.R
import com.josense.locktask.domain.model.Task
import com.josense.locktask.ui.components.LockNotifyStatus
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

private const val TASK_HIGHLIGHT_DURATION_MILLIS = 1_500L


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onTaskClick: (Task) -> Unit = {},
    homeViewModel: HomeViewModel = koinViewModel(),
) {

    val state by homeViewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        modifier = modifier,
        onAction = { action ->
            when (action) {
                is HomeAction.TaskClick -> onTaskClick(action.task)
                else -> homeViewModel.onAction(action)
            }
        },
    )
}

@Composable
fun HomeScreen(
    state: HomeState,
    modifier: Modifier = Modifier,
    onAction: (HomeAction) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val nowMillis = currentTaskTimeMillis()
    val tasks = (state.list as? HomeListState.Content)?.tasks.orEmpty()
    val taskId = state.pendingAnchorTaskId
    val targetIndex = tasks.indexOfFirst { it.id == taskId }
    val removingIds = state.removingTaskIds
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var exiting by remember(removingIds) { mutableStateOf(false) }
    LaunchedEffect(removingIds, lifecycle) {
        if (removingIds.isNotEmpty()) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                exiting = false
                delay(TaskRemovalTiming.HOLD_MILLIS.milliseconds)
                exiting = true
                delay(TaskRemovalTiming.REMOVAL_ACK_DELAY_MILLIS.milliseconds)
                onAction(HomeAction.RemovalFinished(removingIds))
            }
        }
    }
    var highlightedId by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(highlightedId) {
        if (highlightedId != null) {
            delay(TASK_HIGHLIGHT_DURATION_MILLIS.milliseconds)
            highlightedId = null
        }
    }
    LaunchedEffect(taskId, targetIndex) {
        if (taskId != null && targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
            highlightedId = taskId
            onAction(HomeAction.AnchorHandled(taskId))
        }
    }
    when (val list = state.list) {
        HomeListState.Loading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        HomeListState.Empty -> EmptyHomeScreen(modifier)
        is HomeListState.Content -> LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(list.tasks, key = { task -> task.id }) { task ->
                RemovingTaskItem(visible = !(exiting && task.id in removingIds)) {
                    TaskItem(
                        task = task,
                        highlighted = task.id == highlightedId,
                        enabled = task.id !in removingIds,
                        nowMillis = nowMillis,
                        onTaskClick = { onAction(HomeAction.TaskClick(it)) },
                    )
                }
            }
            item(key = "home_list_footer") {
                Text(
                    text = stringResource(R.string.home_list_end),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 88.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}


@Composable
fun EmptyHomeScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EmptyTaskIllustration(
                modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.no_not_completed_task),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    enabled: Boolean = true,
    highlighted: Boolean = false,
    nowMillis: Long = System.currentTimeMillis(),
    onTaskClick: (Task) -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(
                if (highlighted) MaterialTheme.colorScheme.secondaryContainer
                else Color.Transparent
            )
            .clickable(enabled = enabled) { onTaskClick(task) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        LockNotifyStatus(needLockNotify = task.needLockNotify)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = task.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
            )
            TaskCreatedTime(createdAt = task.createAt, nowMillis = nowMillis)
        }
    }
}
