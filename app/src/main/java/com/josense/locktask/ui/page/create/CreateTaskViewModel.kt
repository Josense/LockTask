package com.josense.locktask.ui.page.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.repository.TaskRepository
import kotlinx.coroutines.launch

class CreateTaskViewModel(
    private val taskRepository: TaskRepository
): ViewModel() {
    fun createTask(task: Task) {
        viewModelScope.launch {
            taskRepository.insertTask(task)
        }
    }
}