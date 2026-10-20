package com.josense.locktask.ui.page.home

import com.josense.locktask.domain.model.Task

sealed interface HomeListState {
    data object Loading : HomeListState
    data object Empty : HomeListState
    data class Content(val tasks: List<Task>) : HomeListState
}


data class HomeState(
    val list: HomeListState = HomeListState.Loading,
    val pendingAnchorTaskId: Int? = null,
    val removingTaskIds: Set<Int> = emptySet(),
)

// Keep disappearing rows at their previous indices until the UI finishes their exit.
internal fun HomeState.withTasks(tasks: List<Task>): HomeState {
    val previous = (list as? HomeListState.Content)?.tasks.orEmpty()
    val activeIds = tasks.mapTo(mutableSetOf()) { it.id }
    val removed = previous.filter { it.id !in activeIds }
    val displayed = tasks.toMutableList()
    previous.forEachIndexed { index, task ->
        if (task.id !in activeIds) displayed.add(index.coerceAtMost(displayed.size), task)
    }
    return copy(
        list = if (displayed.isEmpty()) HomeListState.Empty else HomeListState.Content(displayed),
        removingTaskIds = removed.mapTo(mutableSetOf()) { it.id },
    )
}
