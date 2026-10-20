package com.josense.locktask.ui.page.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josense.locktask.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class HomeViewModel(taskRepository: TaskRepository) : ViewModel() {
    private val pendingAnchorTaskId = MutableStateFlow<Int?>(null)
    val state = combine(
        taskRepository.getAllActiveTasks().map { tasks ->
            if (tasks.isEmpty()) HomeListState.Empty else HomeListState.Content(tasks)
        },
        pendingAnchorTaskId,
    ) { list, taskId -> HomeState(list, taskId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.AnchorTask -> pendingAnchorTaskId.value = action.taskId
            is HomeAction.AnchorHandled -> pendingAnchorTaskId.update {
                if (it == action.taskId) null else it
            }
            is HomeAction.TaskClick -> Unit
        }
    }
}
