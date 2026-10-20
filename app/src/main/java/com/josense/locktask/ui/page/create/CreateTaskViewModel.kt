package com.josense.locktask.ui.page.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.repository.TaskRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateTaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(CreateTaskState())
    val state = mutableState.asStateFlow()

    fun onAction(action: CreateTaskAction) {
        when (action) {
            is CreateTaskAction.TitleChanged -> mutableState.update {
                if (it.submission == Submission.Saving || it.submission is Submission.Succeeded) it
                else it.copy(title = action.title, submission = Submission.Idle)
            }
            CreateTaskAction.ToggleLockNotify -> mutableState.update {
                if (it.submission == Submission.Saving || it.submission is Submission.Succeeded) it
                else it.copy(needLockNotify = !it.needLockNotify)
            }
            CreateTaskAction.Submit -> submit()

        }
    }

    private fun submit() {
        val input = state.value
        if (input.submission == Submission.Saving || input.submission is Submission.Succeeded || input.title.isBlank()) return
        mutableState.update { it.copy(submission = Submission.Saving) }
        viewModelScope.launch {
            try {
                val taskId = taskRepository.insertTask(
                    Task(title = input.title, needLockNotify = input.needLockNotify)
                )
                mutableState.update { it.copy(submission = Submission.Succeeded(taskId)) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(submission = Submission.Failed) }
            }
        }
    }
}
