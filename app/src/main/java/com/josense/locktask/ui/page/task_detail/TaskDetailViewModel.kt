package com.josense.locktask.ui.page.task_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.josense.locktask.domain.repository.TaskRepository
import com.josense.locktask.ui.navigation.TaskDetail
import java.util.Date
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val taskDetailRoute = savedStateHandle.toRoute<TaskDetail>()

    private val mutableUiState = MutableStateFlow(TaskDetailState())
    val uiState = mutableUiState.asStateFlow().also {
        taskRepository.getTaskById(taskDetailRoute.taskId)
            .onEach { task ->
                mutableUiState.update { it.copy(task = task) }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: TaskDetailAction) {
        if (action == TaskDetailAction.DismissSaveError) {
            mutableUiState.update { it.copy(saveFailed = false) }
            return
        }
        if (mutableUiState.value.isSaving) return
        val task = mutableUiState.value.task ?: return
        mutableUiState.update {
            it.copy(isSaving = true, saveFailed = false)
        }
        viewModelScope.launch {
            try {
                when (action) {
                    TaskDetailAction.DismissSaveError -> Unit
                    is TaskDetailAction.SetLockNotify -> {
                        if (task.isDeleted.not()) {
                            taskRepository.updateTask(
                                task.copy(
                                    needLockNotify = action.enabled,
                                    updateAt = Date(),
                                )
                            )
                        }
                    }
                    TaskDetailAction.Complete -> {
                        taskRepository.completeTask(task)
                        mutableUiState.update { it.copy(isFinished = true) }
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableUiState.update { it.copy(saveFailed = true) }
            } finally {
                mutableUiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
