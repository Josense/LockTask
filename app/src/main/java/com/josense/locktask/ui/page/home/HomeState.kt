package com.josense.locktask.ui.page.home

import com.josense.locktask.domain.model.Task

sealed interface HomeListState {
    data object Loading : HomeListState
    data object Empty : HomeListState
    data class Content(val tasks: List<Task>) : HomeListState
}

enum class TaskRemovalDirection { Left, Right }

enum class TaskRemovalKind(val direction: TaskRemovalDirection) {
    Complete(TaskRemovalDirection.Left),
    Delete(TaskRemovalDirection.Right),
}

sealed interface TaskOperation {
    val id: Long
    val kind: TaskRemovalKind

    data class Saving(override val id: Long, override val kind: TaskRemovalKind) : TaskOperation
    data class Removing(override val id: Long, override val kind: TaskRemovalKind) : TaskOperation
    data class Failed(override val id: Long, override val kind: TaskRemovalKind) : TaskOperation
}

data class HomeState(
    val list: HomeListState = HomeListState.Loading,
    val pendingAnchorTaskId: Int? = null,
    val operations: Map<Int, TaskOperation> = emptyMap(),
) {
    val removingTaskIds: Set<Int>
        get() = operations.filterValues { it is TaskOperation.Removing }.keys
}

// Preserve the pre-removal snapshot for animation and undo, with one operation per row.
internal fun HomeState.withTasks(tasks: List<Task>, removals: Map<Int, TaskOperation.Removing>): HomeState {
    val previous = (list as? HomeListState.Content)?.tasks.orEmpty()
    val activeIds = tasks.mapTo(mutableSetOf()) { it.id }
    val displayed = tasks.toMutableList()
    previous.forEachIndexed { index, task ->
        if (task.id !in activeIds) displayed.add(index.coerceAtMost(displayed.size), task)
    }
    val nextOperations = operations.filter { (id, operation) ->
        id !in activeIds || operation !is TaskOperation.Removing
    } + removals.filterKeys { it !in activeIds }
    return copy(
        list = if (displayed.isEmpty()) HomeListState.Empty else HomeListState.Content(displayed),
        operations = nextOperations.filterKeys { id -> displayed.any { it.id == id } },
    )
}

internal fun HomeState.finishRemoval(taskId: Int, operationId: Long): HomeState {
    val operation = operations[taskId] as? TaskOperation.Removing ?: return this
    if (operation.id != operationId) return this
    val tasks = (list as? HomeListState.Content)?.tasks.orEmpty().filterNot { it.id == taskId }
    return copy(
        list = if (tasks.isEmpty()) HomeListState.Empty else HomeListState.Content(tasks),
        operations = operations - taskId,
    )
}
