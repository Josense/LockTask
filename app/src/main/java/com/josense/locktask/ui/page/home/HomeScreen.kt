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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.josense.locktask.R
import com.josense.locktask.domain.model.Task
import com.josense.locktask.ui.components.LockNotifyIconBtn
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onTaskClick: (Task) -> Unit = {},
) {
    val homeViewModel: HomeViewModel = koinViewModel()

    val taskList by homeViewModel.taskListState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (taskList.isEmpty()) {
            item {
                EmptyHomeScreen()
            }
        } else {
            items(
                taskList,
                key = { task -> task.id }
            ) { task ->
                TaskItem(task = task, onTaskClick = onTaskClick)
            }
        }
    }
}


@Composable
fun EmptyHomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
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
            .clickable(onClick = {
                onTaskClick(task)
            }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LockNotifyIconBtn(
            needLockNotify = task.needLockNotify,
        )
        Spacer(modifier = Modifier.padding(4.dp))

        Text(
            text = task.title,
        )
    }
}
