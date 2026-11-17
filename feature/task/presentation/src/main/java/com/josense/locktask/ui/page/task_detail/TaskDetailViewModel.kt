package com.josense.locktask.ui.page.task_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.repository.TaskRepository
import com.josense.locktask.domain.repository.TaskUnavailableException
import java.util.Date
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskDetailViewModel(private val taskRepository: TaskRepository, savedStateHandle: SavedStateHandle) :
    ViewModel() {
    private val taskId: Int = requireNotNull(savedStateHandle["taskId"])

    private val mutableUiState = MutableStateFlow(TaskDetailState())
    val uiState = mutableUiState.asStateFlow().also {
        taskRepository.getTaskById(taskId)
            .onEach { task ->
                mutableUiState.update { it.copy(task = task) }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: TaskDetailAction) {
        val state = mutableUiState.value
        if (state.isSaving && action != TaskDetailAction.DismissSaveError) return
        when (action) {
            TaskDetailAction.DismissSaveError -> mutableUiState.update { it.copy(saveFailed = false) }
            is TaskDetailAction.StartEditing -> {
                val task = state.task ?: return
                if (task.isDeleted.not()) {
                    mutableUiState.update {
                        it.copy(
                            edit = it.edit?.copy(field = action.field) ?: TaskDetailEdit(
                                action.field,
                                task.title,
                                task.content,
                            ),
                        )
                    }
                }
            }
            is TaskDetailAction.TitleChanged -> mutableUiState.update {
                it.copy(edit = it.edit?.copy(title = action.title, titleError = false))
            }
            is TaskDetailAction.DescriptionChanged -> mutableUiState.update {
                it.copy(edit = it.edit?.copy(description = action.description))
            }
            TaskDetailAction.FinishEditing -> saveEdit()
            is TaskDetailAction.SetLockNotify -> saveTaskAction { task ->
                if (task.isDeleted.not()) {
                    taskRepository.updateTask(task.copy(needLockNotify = action.enabled, updateAt = Date()))
                }
            }
            TaskDetailAction.Delete -> saveTaskAction(finishDetail = true) { task ->
                taskRepository.deleteTaskSoftly(task)
            }
            TaskDetailAction.Complete -> saveTaskAction(finishDetail = true) { task ->
                taskRepository.completeTask(task)
            }
        }
    }

    private fun saveTaskAction(finishDetail: Boolean = false, save: suspend (Task) -> Unit) {
        val state = mutableUiState.value
        if (state.edit != null) return
        val task = state.task ?: return
        launchSave {
            save(task)
            if (finishDetail) mutableUiState.update { it.copy(isFinished = true) }
        }
    }

    private fun saveEdit() {
        val edit = mutableUiState.value.edit ?: return
        if (edit.title.isBlank()) {
            mutableUiState.update { it.copy(edit = edit.copy(titleError = true)) }
            return
        }
        launchSave {
            val current = taskRepository.getTaskById(taskId).first()
                ?: throw TaskUnavailableException()
            if (current.isDeleted) throw TaskUnavailableException()
            val changed = current.title != edit.title || current.content != edit.description
            if (changed) {
                taskRepository.updateTaskText(current.id, edit.title, edit.description)
            }
            mutableUiState.update { it.copy(edit = null) }
        }
    }

    private fun launchSave(save: suspend () -> Unit) {
        mutableUiState.update { it.copy(isSaving = true, saveFailed = false) }
        viewModelScope.launch {
            try {
                save()
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
