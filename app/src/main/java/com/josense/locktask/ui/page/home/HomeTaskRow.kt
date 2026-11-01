package com.josense.locktask.ui.page.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.josense.locktask.domain.model.Task

@Composable
internal fun HomeTaskRow(
    task: Task,
    operation: TaskOperation?,
    highlighted: Boolean,
    nowMillis: Long,
    onAction: (HomeAction) -> Unit,
) {
    var swiped by remember(task.id) { mutableStateOf(false) }
    val failureId = (operation as? TaskOperation.Failed)?.id
    LaunchedEffect(failureId) {
        if (failureId != null) swiped = false
    }
    val removal = operation as? TaskOperation.Removing
    val enabled = operation == null || operation is TaskOperation.Failed
    RemovingTaskItem(
        visible = removal == null,
        alreadySwiped = swiped,
        operationId = removal?.id,
        direction = removal?.kind?.direction ?: TaskRemovalDirection.Right,
        onFinished = {
            if (removal != null) onAction(HomeAction.RemovalFinished(task.id, removal.id))
        },
    ) {
        SwipeableTaskItem(
            enabled = enabled,
            failureId = failureId,
            onSwiped = { swiped = true },
            onComplete = { onAction(HomeAction.CompleteTask(task.id)) },
            onDelete = { onAction(HomeAction.DeleteTask(task.id)) },
        ) {
            TaskItem(task, enabled, highlighted, nowMillis) { onAction(HomeAction.TaskClick(it)) }
        }
    }
}
