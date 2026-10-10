package com.josense.locktask.domain.repository

import com.josense.locktask.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>

    suspend fun insertTask(task: Task)

    suspend fun updateTask(task: Task)

    suspend fun completeTask(task: Task, isCompleted: Boolean = true)

    suspend fun deleteTaskSoftly(task: Task)

    suspend fun clearTrash()

}