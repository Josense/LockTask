package com.josense.locktask.ui.page.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.repository.TaskRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(private val taskRepository: TaskRepository) : ViewModel() {
    private val removedTasks = Channel<Task>(Channel.CONFLATED)
    val removals = removedTasks.receiveAsFlow()
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
            is HomeAction.RemovalFinished -> {
                val current = mutableState.value
                val removed = (current.list as? HomeListState.Content)?.tasks?.lastOrNull {
                    it.id in action.taskIds && it.id in current.removingTaskIds
                }
                mutableState.update { current ->
                    val removedIds = action.taskIds intersect current.removingTaskIds
                    val displayed = (current.list as? HomeListState.Content)?.tasks.orEmpty()
                    val tasks = displayed.filterNot { it.id in removedIds }
                    current.copy(
                        list = if (tasks.isEmpty()) HomeListState.Empty else HomeListState.Content(tasks),
                        removingTaskIds = current.removingTaskIds - removedIds,
                    )
                }
                if (removed != null) removedTasks.trySend(removed)
            }
            is HomeAction.UndoRemoval -> undoRemoval(action.task)
            is HomeAction.TaskClick -> Unit
        }
    }

    private fun undoRemoval(snapshot: Task) {
        viewModelScope.launch {
            try {
                val task = taskRepository.getTaskById(snapshot.id).first()
                    ?: error("Removed task no longer exists")
                taskRepository.updateTask(
                    task.copy(
                        isCompleted = snapshot.isCompleted,
                        isDeleted = snapshot.isDeleted,
                    ),
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.w("HomeViewModel", "Unable to undo task removal", error)
            }
        }
    }
}
