package com.josense.locktask.ui.page.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josense.locktask.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(taskRepository: TaskRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(HomeState())
    val state = flow {
        coroutineScope {
            launch {
                taskRepository.getAllActiveTasks().collect { tasks ->
                    mutableState.update { it.withTasks(tasks) }
                }
            }
            emitAll(mutableState)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.AnchorTask -> mutableState.update { it.copy(pendingAnchorTaskId = action.taskId) }
            is HomeAction.AnchorHandled -> mutableState.update {
                if (it.pendingAnchorTaskId == action.taskId) it.copy(pendingAnchorTaskId = null) else it
            }
            is HomeAction.RemovalFinished -> mutableState.update { current ->
                val removedIds = action.taskIds intersect current.removingTaskIds
                val tasks = (current.list as? HomeListState.Content)?.tasks.orEmpty()
                    .filterNot { it.id in removedIds }
                current.copy(
                    list = if (tasks.isEmpty()) HomeListState.Empty else HomeListState.Content(tasks),
                    removingTaskIds = current.removingTaskIds - removedIds,
                )
            }
            is HomeAction.TaskClick -> Unit
        }
    }
}
