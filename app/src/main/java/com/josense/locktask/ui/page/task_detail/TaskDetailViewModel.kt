package com.josense.locktask.ui.page.task_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.repository.TaskRepository
import com.josense.locktask.ui.navigation.TaskDetail
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val taskDetailRoute = savedStateHandle.toRoute<TaskDetail>()

    val taskListState: StateFlow<Task?> = taskRepository.getTaskById(taskDetailRoute.taskId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun onAction(action: TaskDetailAction) {
        when (action) {
            TaskDetailAction.Complete -> {
                viewModelScope.launch {
                    taskRepository.completeTask(taskListState.value!!)
                }
            }

            TaskDetailAction.Delete -> {
                viewModelScope.launch {
                    taskRepository.deleteTaskSoftly(taskListState.value!!)
                }
            }
        }
    }
}