package com.josense.locktask.ui.page.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.josense.locktask.R
import com.josense.locktask.domain.model.Task
import com.josense.locktask.ui.components.LockNotifyStatus
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onTaskClick: (Task) -> Unit = {},
) {
    val homeViewModel: HomeViewModel = koinViewModel()

    val state by homeViewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        modifier = modifier,
        onAction = { action ->
            when (action) {
                is HomeAction.TaskClick -> onTaskClick(action.task)
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
    when (state) {
        HomeState.Loading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        HomeState.Empty -> EmptyHomeScreen(modifier)
        is HomeState.Content -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.tasks, key = { task -> task.id }) { task ->
                TaskItem(task = task, onTaskClick = { onAction(HomeAction.TaskClick(it)) })
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
        Text(text = stringResource(R.string.no_not_completed_task))
    }
}

@Composable
fun TaskItem(
    task: Task,
    onTaskClick: (Task) -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable { onTaskClick(task) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LockNotifyStatus(needLockNotify = task.needLockNotify)
        Spacer(modifier = Modifier.padding(4.dp))
        Text(text = task.title)
    }
}
