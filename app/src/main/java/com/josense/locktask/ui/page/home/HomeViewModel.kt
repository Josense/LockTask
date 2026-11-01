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
    private var nextOperationId = 0L
    private val failedActions = Channel<Unit>(Channel.CONFLATED)
    val failures = failedActions.receiveAsFlow()
    private val removedTasks = Channel<Task>(Channel.CONFLATED)
    val removals = removedTasks.receiveAsFlow()
    private val mutableState = MutableStateFlow(HomeState())
    val state = flow {
        coroutineScope {
            launch {
                taskRepository.getAllActiveTasks().collect { tasks ->
                    val current = mutableState.value
                    val activeIds = tasks.mapTo(mutableSetOf()) { it.id }
                    val previous = (current.list as? HomeListState.Content)?.tasks.orEmpty()
                    val removals = previous
                        .filter { it.id !in activeIds && current.operations[it.id] !is TaskOperation.Removing }
                        .associate { previousTask ->
                            val pending = current.operations[previousTask.id] as? TaskOperation.Saving
                            val kind = pending?.kind ?: run {
                                val stored = taskRepository.getTaskById(previousTask.id).first()
                                if (stored?.isCompleted == true && stored.isDeleted.not()) {
                                    TaskRemovalKind.Complete
                                } else {
                                    TaskRemovalKind.Delete
                                }
                            }
                            previousTask.id to TaskOperation.Removing(pending?.id ?: ++nextOperationId, kind)
                        }
                    mutableState.update { it.withTasks(tasks, removals) }
                }
            }
            emitAll(mutableState)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.CompleteTask -> removeTask(action.taskId, TaskRemovalKind.Complete)
            is HomeAction.DeleteTask -> removeTask(action.taskId, TaskRemovalKind.Delete)
            is HomeAction.AnchorTask -> mutableState.update { it.copy(pendingAnchorTaskId = action.taskId) }
            is HomeAction.AnchorHandled -> mutableState.update {
                if (it.pendingAnchorTaskId == action.taskId) it.copy(pendingAnchorTaskId = null) else it
            }
            is HomeAction.RemovalFinished -> {
                val current = mutableState.value
                val next = current.finishRemoval(action.taskId, action.operationId)
                if (next != current) {
                    val removed = (current.list as? HomeListState.Content)?.tasks?.find { it.id == action.taskId }
                    mutableState.value = next
                    if (removed != null) removedTasks.trySend(removed)
                }
            }
            is HomeAction.UndoRemoval -> undoRemoval(action.task)
            is HomeAction.TaskClick -> Unit
        }
    }

    private fun removeTask(taskId: Int, kind: TaskRemovalKind) {
        val current = mutableState.value
        val existing = current.operations[taskId]
        if (existing is TaskOperation.Saving || existing is TaskOperation.Removing) return
        if ((current.list as? HomeListState.Content)?.tasks?.none { it.id == taskId } != false) return
        val operation = TaskOperation.Saving(++nextOperationId, kind)
        mutableState.update { it.copy(operations = it.operations + (taskId to operation)) }
        viewModelScope.launch {
            try {
                val task = taskRepository.getTaskById(taskId).first()
                    ?: error("Task no longer exists")
                if (task.isCompleted.not() && task.isDeleted.not()) {
                    when (kind) {
                        TaskRemovalKind.Complete -> taskRepository.completeTask(task)
                        TaskRemovalKind.Delete -> taskRepository.deleteTaskSoftly(task)
                    }
                }
                // Keep Saving until the repository confirms removal from the active list.
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.update {
                    if (it.operations[taskId] == operation) {
                        it.copy(operations = it.operations + (taskId to TaskOperation.Failed(operation.id, kind)))
                    } else {
                        it
                    }
                }
                Log.w("HomeViewModel", "Unable to remove task", error)
                failedActions.trySend(Unit)
            }
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
