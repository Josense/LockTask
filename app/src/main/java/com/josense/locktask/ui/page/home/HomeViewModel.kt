package com.josense.locktask.ui.page.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josense.locktask.domain.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val taskRepository: TaskRepository
): ViewModel() {

    val state: StateFlow<HomeState> = taskRepository.getAllActiveTasks()
        .map { tasks ->
            if (tasks.isEmpty()) HomeState.Empty else HomeState.Content(tasks)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeState.Loading
        )
}
